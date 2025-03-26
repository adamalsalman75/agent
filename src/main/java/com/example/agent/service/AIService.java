package com.example.agent.service;

import com.example.agent.service.task.TaskAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AIService {
    private final ChatClient chatClient;
    private final List<TaskAction> taskActions;

    public AIService(ChatClient chatClient, List<TaskAction> taskActions) {
        this.chatClient = chatClient;
        this.taskActions = taskActions;
    }

    public Map<String, Object> processQuery(String query) {
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
        
        Map<String, Object> context = buildContext(parts);
        
        return taskActions.stream()
                .filter(action -> action.canHandle(intent))
                .findFirst()
                .map(action -> action.execute(context))
                .orElseThrow(() -> new RuntimeException("No handler found for intent: " + intent));
    }

    private Map<String, Object> buildContext(String[] parts) {
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