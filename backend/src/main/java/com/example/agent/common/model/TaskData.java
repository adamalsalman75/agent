package com.example.agent.common.model;

/**
 * Record to represent collected data about a task.
 * This replaces the use of JsonNode for more type-safe operations.
 */
public record TaskData(
    String description,
    String deadline,
    String priority,
    String constraints,
    String taskId
) {
    public static TaskData createEmpty() {
        return new TaskData(null, null, null, null, null);
    }
    
    /**
     * Creates a new TaskData with only a description.
     */
    public static TaskData createWithDescription(String description) {
        return new TaskData(description, null, null, null, null);
    }
    
    /**
     * Creates a new TaskData for a task completion with just the taskId.
     */
    public static TaskData createForCompletion(String taskId) {
        return new TaskData(null, null, null, null, taskId);
    }
}