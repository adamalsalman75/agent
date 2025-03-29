package com.example.agent.task.service.action;

public record TaskParameters(
        String description,
        String deadline,
        String priority,
        String constraints,
        String taskId
) {
    public static TaskParameters forCreateTask(
            String taskId,
            String description,
            String deadline,
            String priority,
            String constraints
    ) {
        return new TaskParameters(description, deadline, priority, constraints, taskId);
    }

    public static TaskParameters forCompleteTask(String taskId) {
        return new TaskParameters(null, null, null, null, taskId);
    }
}