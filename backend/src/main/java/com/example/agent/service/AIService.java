package com.example.agent.service;

import com.example.agent.model.ConversationContext;
import com.example.agent.model.QueryRequest;
import com.example.agent.model.QueryResponse;
import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import com.example.agent.service.reasoning.Refinement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AIService {
    private final TaskRepository taskRepository;
    private final Refinement refinement;
    private final ObjectMapper objectMapper;
    private final CompletionService completionService;

    public AIService(TaskRepository taskRepository, Refinement refinement, ObjectMapper objectMapper, CompletionService completionService) {
        this.taskRepository = taskRepository;
        this.refinement = refinement;
        this.objectMapper = objectMapper;
        this.completionService = completionService;
    }

    public QueryResponse processQuery(QueryRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }
            
            String query = request.query();
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Query cannot be null or empty");
            }
            
            ConversationContext context = refinement.refineTask(query, request.context());
            if (context == null) {
                return new QueryResponse("Failed to process query", null, false, null);
            }
            
            if (context.requiresFollowUp()) {
                return new QueryResponse(
                    context.getNextPrompt(),
                    null,
                    true,
                    context
                );
            }
            
            Task resultTask = null;
            if (context.getCurrentIntent() != null) {
                try {
                    switch (context.getCurrentIntent()) {
                        case "CREATE_TASK" -> resultTask = createTaskFromContext(context.getCollectedData());
                        case "UPDATE_TASK" -> resultTask = updateTaskFromContext(context);
                        case "TASK_COMPLETE" -> {
                            JsonNode data = context.getCollectedData();
                            if (data != null && data.has("taskId")) {
                                Long taskId = Long.parseLong(data.get("taskId").asText());
                                resultTask = taskRepository.findById(taskId)
                                    .map(Task::markCompleted)
                                    .map(taskRepository::save)
                                    .orElseThrow(() -> new IllegalArgumentException("Task not found"));
                            } else {
                                throw new IllegalArgumentException("Task ID is required for completion");
                            }
                        }
                        default -> {
                            return new QueryResponse("Unknown action: " + context.getCurrentIntent(), null, false, null);
                        }
                    }
                } catch (Exception e) {
                    return new QueryResponse("Error processing task: " + e.getMessage(), null, false, null);
                }
            } else {
                return new QueryResponse("Failed to process query: No intent recognized", null, false, null);
            }
            
            return new QueryResponse(
                context.getNextPrompt() != null ? context.getNextPrompt() : "Task processed successfully",
                resultTask,
                false,
                null
            );
        } catch (IllegalArgumentException e) {
            throw e;  // Let validation exceptions propagate
        } catch (Exception e) {
            return new QueryResponse("Error processing query: " + e.getMessage(), null, false, null);
        }
    }

    private Task createTaskFromContext(JsonNode data) {
        if (data == null) {
            throw new IllegalArgumentException("Task data cannot be null");
        }

        if (!data.has("description")) {
            throw new IllegalArgumentException("Task description is required");
        }

        String description = data.get("description").asText();
        LocalDateTime deadline = data.has("deadline") ? 
            LocalDateTime.parse(data.get("deadline").asText()) : 
            null;
        String priority = data.has("priority") ? 
            data.get("priority").asText() : 
            null;
        String constraints = data.has("constraints") ? 
            data.get("constraints").asText() : 
            null;

        Task task = Task.createNewWithDetails(
            description,
            deadline,
            priority,
            constraints,
            null,
            null
        );
        task = taskRepository.save(task);
        Long parentId = task.id();

        // Handle subtasks if present
        if (data.has("subtasks") && data.get("subtasks").isArray()) {
            ArrayNode subtasks = (ArrayNode) data.get("subtasks");
            for (JsonNode subtaskData : subtasks) {
                ((ObjectNode) subtaskData).put("parentId", parentId);
                Task subtask = createSubtaskFromContext(subtaskData, parentId);
                taskRepository.save(subtask);
            }
        }

        return task;
    }

    private Task createSubtaskFromContext(JsonNode data, Long parentId) {
        if (data == null) {
            throw new IllegalArgumentException("Subtask data cannot be null");
        }

        if (!data.has("description")) {
            throw new IllegalArgumentException("Subtask description is required");
        }

        String description = data.get("description").asText();
        LocalDateTime deadline = data.has("deadline") ? 
            LocalDateTime.parse(data.get("deadline").asText()) : 
            null;
        String priority = data.has("priority") ? 
            data.get("priority").asText() : 
            null;
        String constraints = data.has("constraints") ? 
            data.get("constraints").asText() : 
            null;

        return Task.createNewWithDetails(
            description,
            deadline,
            priority,
            constraints,
            parentId,
            null
        );
    }

    private Task updateTaskFromContext(ConversationContext context) {
        if (context == null || context.getCollectedData() == null) {
            throw new IllegalArgumentException("Context data cannot be null");
        }

        Task existingTask = context.getInProgressTask();
        if (existingTask == null) {
            throw new IllegalArgumentException("Existing task not found");
        }

        Task updatedTask = existingTask.updateFromContext(context.getCollectedData());
        return taskRepository.save(updatedTask);
    }
}