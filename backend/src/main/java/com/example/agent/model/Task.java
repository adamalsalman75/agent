package com.example.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

@Table("tasks")
public record Task(
    @Id
    Long id,
    
    String description,
    
    boolean completed,
    
    @Column("created_at")
    LocalDateTime createdAt,
    
    @Column("completed_at")
    LocalDateTime completedAt,
    
    LocalDateTime deadline,
    
    String priority,
    
    String constraints,
    
    @Column("parent_id")
    Long parentId,
    
    JsonNode metadata
) {
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
    
    public Task markCompleted() {
        return new Task(
            this.id,
            this.description,
            true,
            this.createdAt,
            LocalDateTime.now(),
            this.deadline,
            this.priority,
            this.constraints,
            this.parentId,
            this.metadata
        );
    }

    public Task updateMetadata(JsonNode newMetadata) {
        return new Task(
            this.id,
            this.description,
            this.completed,
            this.createdAt,
            this.completedAt,
            this.deadline,
            this.priority,
            this.constraints,
            this.parentId,
            newMetadata
        );
    }
}