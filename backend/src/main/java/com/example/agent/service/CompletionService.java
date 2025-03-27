package com.example.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CompletionService {
    private final ChatClient chatClient;

    public CompletionService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String getCompletion(String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}