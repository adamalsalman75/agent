package com.example.agent.service;

import com.example.agent.model.ConversationContext;
import com.example.agent.model.QueryRequest;
import com.example.agent.model.QueryResponse;
import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import com.example.agent.service.reasoning.Refinement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AIService {
    private final TaskRepository taskRepository;
    private final Refinement refinement;
    private final ObjectMapper objectMapper;
    private final CompletionService completionService;

    public AIService(
            TaskRepository taskRepository,
            Refinement refinement,
            ObjectMapper objectMapper,
            CompletionService completionService) {
        this.taskRepository = taskRepository;
        this.refinement = refinement;
        this.objectMapper = objectMapper;
        this.completionService = completionService;
    }

    @Transactional
    public QueryResponse processQuery(QueryRequest request) {
        ConversationContext context = refinement.refineTask(request.query(), request.context());

        if (context.requiresFollowUp()) {
            return QueryResponse.needsMoreInfo(
                context.nextPrompt(),
                objectMapper.valueToTree(context)
            );
        }

        // Check if this is an update to an existing task
        if (context.inProgressTask() != null) {
            Task updatedTask = updateTaskFromContext(context.inProgressTask(), context);
            if (updatedTask != null) {
                return QueryResponse.withTask(
                    "Successfully updated task: " + updatedTask.description(),
                    updatedTask
                );
            }
        }

        // If not an update, create new task
        Task task = createTaskFromContext(context);
        if (task != null) {
            task = taskRepository.save(task);
            return QueryResponse.withTask(
                "Successfully created task: " + task.description(),
                task
            );
        }

        return QueryResponse.success("I couldn't process that input. Could you try rephrasing?");
    }

    private Task createTaskFromContext(ConversationContext context) {
        try {
            JsonNode data = context.collectedData();
            String description = data.get("description").asText();
            Task task = Task.createNewWithDetails(
                description,
                data.has("deadline") ? parseDateTime(data.get("deadline").asText()) : null,
                data.has("priority") ? data.get("priority").asText() : null,
                data.has("constraints") ? data.get("constraints").asText() : null,
                data.has("parentId") ? data.get("parentId").asLong() : null,
                data.has("metadata") ? data.get("metadata") : null
            );

            // If this task has subtasks, create them recursively
            if (data.has("subtasks") && data.get("subtasks").isArray()) {
                task = taskRepository.save(task); // Save parent first
                for (JsonNode subtask : data.get("subtasks")) {
                    var subtaskContext = new ConversationContext(
                        null, subtask, false, null, null
                    );
                    Task childTask = createTaskFromContext(subtaskContext);
                    if (childTask != null) {
                        childTask = Task.createNewWithDetails(
                            childTask.description(),
                            childTask.deadline(),
                            childTask.priority(),
                            childTask.constraints(),
                            task.id(), // Set parent ID
                            childTask.metadata()
                        );
                        taskRepository.save(childTask);
                    }
                }
            }

            return task;
        } catch (Exception e) {
            return null;
        }
    }

    private Task updateTaskFromContext(Task existingTask, ConversationContext context) {
        try {
            JsonNode data = context.collectedData();
            return taskRepository.save(existingTask.updateFromContext(data));
        } catch (Exception e) {
            return null;
        }
    }

    private java.time.LocalDateTime parseDateTime(String dateStr) {
        try {
            return java.time.LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}