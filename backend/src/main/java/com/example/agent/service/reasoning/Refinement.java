package com.example.agent.service.reasoning;

import com.example.agent.model.ConversationContext;
import com.example.agent.model.Task;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class Refinement {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public Refinement(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public ConversationContext refineTask(String query, JsonNode context) {
        // Create a new context if null is provided
        ConversationContext currentContext = context != null ?
            reconstructContext(context) : ConversationContext.createNew();

        // If this is a follow-up response, include previous context
        String prompt = buildPrompt(query, currentContext != null ? currentContext : ConversationContext.createNew());
        String response = chatClient
            .prompt()
            .system("You are a task refinement assistant. Analyze the task and help make it well-defined.")
            .user(prompt)
            .call()
            .content();
        
        return processAIResponse(response, currentContext != null ? currentContext : ConversationContext.createNew());
    }

    private String buildPrompt(String query, ConversationContext context) {
        // Ensure we always have a valid context
        if (context == null) {
            context = ConversationContext.createNew();
        }

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
        // Ensure we always have a valid context
        if (context == null) {
            context = ConversationContext.createNew();
        }
        
        // Handle null content from the AI response
        if (content == null) {
            return new ConversationContext(
                "CREATE_TASK",
                parseTaskData("No task description available"),
                false,
                null,
                context.inProgressTask()
            );
        }
        
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
        
        // For new tasks, set the intent to CREATE_TASK
        if (context.currentIntent() == null) {
            return new ConversationContext(
                "CREATE_TASK",
                parseTaskData(content),
                false,
                null,
                null
            );
        }
        
        // Otherwise, maintain the existing intent
        return new ConversationContext(
            context.currentIntent(),
            parseTaskData(content),
            false,
            null,
            context.inProgressTask()
        );
    }

    private String extractQuestion(String response) {
        // Extract the question from AI response
        // Remove the NEEDS_MORE_INFO marker and any system instructions
        return response.replace("NEEDS_MORE_INFO:", "").trim();
    }

    private JsonNode parseTaskData(String response) {
        try {
            // Create a structured task data object from the AI response
            ObjectNode taskData = objectMapper.createObjectNode();
            taskData.put("description", response.trim());
            return taskData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse task data", e);
        }
    }

    private ConversationContext reconstructContext(JsonNode contextJson) {
        try {
            if (contextJson == null) {
                return ConversationContext.createNew();
            }
            return objectMapper.treeToValue(contextJson, ConversationContext.class);
        } catch (Exception e) {
            // If we fail to reconstruct the context, return a new one instead of throwing
            return ConversationContext.createNew();
        }
    }
}

record RefinementResult(
    Map<String, Object> refinedOutput,
    List<String> followUpQuestions,
    boolean needsMoreFeedback
) {}