package com.example.agent.service.decision;

import com.example.agent.service.task.TaskAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenAIDecisionMaker implements DecisionMaker {
    private final ChatClient chatClient;
    private final List<TaskAction> availableActions;

    public OpenAIDecisionMaker(ChatClient chatClient, List<TaskAction> taskActions) {
        this.chatClient = chatClient;
        this.availableActions = taskActions;
    }

    @Override
    public Optional<ActionDecision> decide(Map<String, Object> context) {
        String query = (String) context.get("query");
        if (query == null) {
            return Optional.empty();
        }

        String systemPromptTemplate = """
            You are a task management assistant that helps users manage their tasks.
            Analyze the user's intent and respond with one of these exact formats:
            - CREATE_TASK|{task description}
            - COMPLETE_TASK|{task id}
            - LIST_TASKS
            Only respond with one of these formats, nothing else.
            """;

        String aiResponse = chatClient
                .prompt()
                .system(systemPromptTemplate)
                .user(query)
                .call()
                .content();

        String[] parts = aiResponse.split("\\|");
        String intent = parts[0];
        
        Map<String, Object> parameters = buildParameters(parts);
        
        return availableActions.stream()
                .filter(action -> action.canHandle(intent))
                .findFirst()
                .map(action -> new ActionDecision(action, parameters));
    }

    private Map<String, Object> buildParameters(String[] parts) {
        if (parts.length > 1) {
            if ("CREATE_TASK".equals(parts[0])) {
                return Map.of("description", parts[1]);
            } else if ("COMPLETE_TASK".equals(parts[0])) {
                return Map.of("taskId", parts[1]);
            }
        }
        return Map.of();
    }
}