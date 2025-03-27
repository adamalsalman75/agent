package com.example.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

public record ConversationContext(
    String currentIntent,
    JsonNode collectedData,
    boolean requiresFollowUp,
    String nextPrompt,
    Task inProgressTask
) {
    public static ConversationContext createNew() {
        return new ConversationContext(null, null, false, null, null);
    }
}