package com.example.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

public record QueryResponse(
    String response,
    boolean requiresFollowUp,
    JsonNode context,
    Task resultTask
) {
    public static QueryResponse success(String response) {
        return new QueryResponse(response, false, null, null);
    }
    
    public static QueryResponse needsMoreInfo(String response, JsonNode context) {
        return new QueryResponse(response, true, context, null);
    }
    
    public static QueryResponse withTask(String response, Task task) {
        return new QueryResponse(response, false, null, task);
    }
}