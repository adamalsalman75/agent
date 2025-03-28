package com.example.agent.model;

public record QueryResponse(
    String response,
    Task resultTask,
    boolean requiresFollowUp,
    ConversationContext context
) {}