package com.example.agent.ai.model;

import com.example.agent.common.model.Task;

public record QueryResponse(
    String response,
    Task resultTask,
    boolean requiresFollowUp,
    ConversationContext context
) {}