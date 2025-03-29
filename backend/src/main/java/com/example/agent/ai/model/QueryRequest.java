package com.example.agent.ai.model;

public record QueryRequest(
    String query,
    ConversationContext context
) {}