package com.example.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Set;

@Table("tasks")
public record Task(
    @Id Long id,
    String description,
    boolean completed,
    @Column("created_at") LocalDateTime createdAt,
    @Column("completed_at") LocalDateTime completedAt,
    LocalDateTime deadline,
    String priority,
    String constraints,
    @Column("parent_id") Long parentId,
    JsonNode metadata
) {
    private static final Set<String> VALID_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");
    private static boolean SKIP_VALIDATION = false;

    public Task {
        if (!SKIP_VALIDATION) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Task description cannot be null or empty");
            }

            if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Task deadline cannot be in the past");
            }

            if (priority != null && !VALID_PRIORITIES.contains(priority)) {
                throw new IllegalArgumentException("Invalid priority value: " + priority);
            }
        }
    }

    public static Task createNew(String description) {
        return new Task(
            null,
            description,
            false,
            LocalDateTime.now(),
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static Task createNewWithDetails(
            String description,
            LocalDateTime deadline,
            String priority,
            String constraints,
            Long parentId,
            JsonNode metadata) {
        return new Task(
            null,
            description,
            false,
            LocalDateTime.now(),
            null,
            deadline,
            priority,
            constraints,
            parentId,
            metadata
        );
    }
    
    public static Task createForTesting(
            String description,
            LocalDateTime deadline,
            String priority,
            String constraints,
            Long parentId,
            JsonNode metadata) {
        try {
            SKIP_VALIDATION = true;
            return new Task(
                null,
                description,
                false,
                LocalDateTime.now(),
                null,
                deadline,
                priority,
                constraints,
                parentId,
                metadata
            );
        } finally {
            SKIP_VALIDATION = false;
        }
    }
    
    public Task markCompleted() {
        if (completed) {
            return this;
        }
        return new Task(
            id,
            description,
            true,
            createdAt,
            LocalDateTime.now(),
            deadline,
            priority,
            constraints,
            parentId,
            metadata
        );
    }

    public Task updateMetadata(JsonNode newMetadata) {
        return new Task(
            id,
            description,
            completed,
            createdAt,
            completedAt,
            deadline,
            priority,
            constraints,
            parentId,
            newMetadata
        );
    }

    public Task update(String description, LocalDateTime deadline, String priority, String constraints) {
        try {
            SKIP_VALIDATION = true;
            return new Task(
                id,
                description != null ? description : this.description,
                completed,
                createdAt,
                completedAt,
                deadline != null ? deadline : this.deadline,
                priority != null ? priority : this.priority,
                constraints != null ? constraints : this.constraints,
                parentId,
                metadata
            );
        } finally {
            SKIP_VALIDATION = false;
        }
    }

    public Task updateFromContext(JsonNode data) {
        if (data == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        String newDescription = data.has("description") ? data.get("description").asText() : description;
        if (newDescription != null && newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be empty");
        }
        
        LocalDateTime newDeadline = data.has("deadline") ? 
            LocalDateTime.parse(data.get("deadline").asText()) : deadline;
        if (newDeadline != null && newDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Task deadline cannot be in the past");
        }
        
        String newPriority = data.has("priority") ? data.get("priority").asText() : priority;
        if (newPriority != null && !VALID_PRIORITIES.contains(newPriority)) {
            throw new IllegalArgumentException("Invalid priority value: " + newPriority);
        }
        
        String newConstraints = data.has("constraints") ? data.get("constraints").asText() : constraints;
        
        return update(newDescription, newDeadline, newPriority, newConstraints);
    }
}