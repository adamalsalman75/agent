package com.example.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("tasks")
public record Task(
    @Id
    Long id,
    
    String description,
    
    boolean completed,
    
    LocalDateTime createdAt,
    
    LocalDateTime completedAt
) {
    // Constructor for creating a new task with default values
    public static Task createNew(String description) {
        return new Task(
            null,
            description,
            false,
            LocalDateTime.now(),
            null
        );
    }
    
    // Method to mark the task as completed
    public Task markCompleted() {
        return new Task(
            this.id,
            this.description,
            true,
            this.createdAt,
            LocalDateTime.now()
        );
    }
}