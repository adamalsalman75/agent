package com.example.agent.service.reasoning;

import com.example.agent.model.ConversationContext;
import com.example.agent.model.Task;
import com.example.agent.service.CompletionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class Refinement {
    private final CompletionService completionService;
    private final ObjectMapper objectMapper;

    public Refinement(CompletionService completionService, ObjectMapper objectMapper) {
        this.completionService = completionService;
        this.objectMapper = objectMapper;
    }

    public ConversationContext refineTask(String query, JsonNode context) {
        ConversationContext currentContext = context != null ?
            reconstructContext(context) : ConversationContext.createNew();

        // If this is a follow-up response, include previous context
        String prompt = buildPrompt(query, currentContext);
        String response = completionService.getCompletion(prompt);
        
        return processAIResponse(response, currentContext);
    }

    private String buildPrompt(String query, ConversationContext context) {
        if (!context.requiresFollowUp()) {
            return """
                You are a task refinement assistant. Your goal is to help create well-defined tasks.
                For each task, try to collect:
                1. Clear description
                2. Deadline (if applicable)
                3. Priority level
                4. Any constraints or requirements
                5. Break down into subtasks if complex

                If you need more information, ask specific questions.
                Current query: %s
                """.formatted(query);
        } else {
            return """
                Previous conversation:
                Intent: %s
                Collected data: %s
                Next question: %s
                User response: %s
                """.formatted(
                    context.currentIntent(),
                    context.collectedData(),
                    context.nextPrompt(),
                    query
                );
        }
    }

    private ConversationContext processAIResponse(String content, ConversationContext context) {
        // Check if the response indicates need for more information
        if (content.contains("NEEDS_MORE_INFO")) {
            return new ConversationContext(
                context.currentIntent(),
                context.collectedData(),
                true,
                extractQuestion(content),
                context.inProgressTask()
            );
        }
        
        // Process completed task information
        return new ConversationContext(
            "TASK_COMPLETE",
            parseTaskData(content),
            false,
            null,
            null
        );
    }

    private String extractQuestion(String response) {
        // Extract the question from AI response
        // Remove the NEEDS_MORE_INFO marker and any system instructions
        return response.replace("NEEDS_MORE_INFO:", "").trim();
    }

    private JsonNode parseTaskData(String response) {
        try {
            ObjectNode taskData = objectMapper.createObjectNode();
            // Parse the structured response into task data
            // This will be used to create or update tasks
            return taskData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse task data", e);
        }
    }

    private ConversationContext reconstructContext(JsonNode contextJson) {
        try {
            return objectMapper.treeToValue(contextJson, ConversationContext.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct conversation context", e);
        }
    }
}

record RefinementResult(
    Map<String, Object> refinedOutput,
    List<String> followUpQuestions,
    boolean needsMoreFeedback
) {}