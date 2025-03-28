package com.example.agent.model;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateNew_WithValidDescription_ShouldCreateTask() {
        String description = "Test task";
        Task task = Task.createNew(description);
        
        assertNotNull(task);
        assertEquals(description, task.description());
        assertFalse(task.completed());
        assertNotNull(task.createdAt());
        assertNull(task.completedAt());
        assertNull(task.deadline());
        assertNull(task.priority());
        assertNull(task.constraints());
        assertNull(task.parentId());
        assertNull(task.metadata());
    }

    @Test
    void testCreateNew_WithNullDescription_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Task.createNew(null));
    }

    @Test
    void testCreateNew_WithBlankDescription_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Task.createNew(""));
        assertThrows(IllegalArgumentException.class, () -> Task.createNew("   "));
    }

    @Test
    void testCreateNewWithDetails_WithValidData_ShouldCreateTask() {
        String description = "Test task with details";
        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        String priority = "HIGH";
        String constraints = "Must be done carefully";
        Long parentId = 1L;
        ObjectNode metadata = objectMapper.createObjectNode()
            .put("category", "test")
            .put("tags", "important");

        Task task = Task.createNewWithDetails(
            description,
            deadline,
            priority,
            constraints,
            parentId,
            metadata
        );

        assertNotNull(task);
        assertEquals(description, task.description());
        assertEquals(deadline, task.deadline());
        assertEquals(priority, task.priority());
        assertEquals(constraints, task.constraints());
        assertEquals(parentId, task.parentId());
        assertEquals("test", task.metadata().get("category").asText());
        assertEquals("important", task.metadata().get("tags").asText());
        assertFalse(task.completed());
        assertNotNull(task.createdAt());
        assertNull(task.completedAt());
    }

    @Test
    void testCreateNewWithDetails_WithPastDeadline_ShouldThrowException() {
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(1);
        
        assertThrows(IllegalArgumentException.class, () ->
            Task.createNewWithDetails(
                "Test task",
                pastDeadline,
                "HIGH",
                null,
                null,
                null
            )
        );
    }

    @Test
    void testCreateNewWithDetails_WithInvalidPriority_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
            Task.createNewWithDetails(
                "Test task",
                LocalDateTime.now().plusDays(1),
                "INVALID",
                null,
                null,
                null
            )
        );
    }

    @Test
    void testMarkCompleted_OnUncompletedTask_ShouldMarkAsCompleted() {
        Task task = Task.createNew("Test task");
        Task completedTask = task.markCompleted();

        assertTrue(completedTask.completed());
        assertNotNull(completedTask.completedAt());
        assertEquals(task.description(), completedTask.description());
        assertEquals(task.deadline(), completedTask.deadline());
        assertEquals(task.priority(), completedTask.priority());
        assertEquals(task.constraints(), completedTask.constraints());
        assertEquals(task.parentId(), completedTask.parentId());
        assertEquals(task.metadata(), completedTask.metadata());
    }

    @Test
    void testMarkCompleted_OnCompletedTask_ShouldReturnSameTask() {
        Task task = Task.createNew("Test task");
        Task completedTask = task.markCompleted();
        Task recompletedTask = completedTask.markCompleted();

        assertEquals(completedTask.completed(), recompletedTask.completed());
        assertEquals(completedTask.completedAt(), recompletedTask.completedAt());
    }

    @Test
    void testUpdate_WithValidData_ShouldUpdateTask() {
        Task originalTask = Task.createNew("Original description");
        String newDescription = "Updated description";
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(2);
        String newPriority = "LOW";
        String newConstraints = "New constraints";

        Task updatedTask = originalTask.update(
            newDescription,
            newDeadline,
            newPriority,
            newConstraints
        );

        assertEquals(newDescription, updatedTask.description());
        assertEquals(newDeadline, updatedTask.deadline());
        assertEquals(newPriority, updatedTask.priority());
        assertEquals(newConstraints, updatedTask.constraints());
        assertEquals(originalTask.id(), updatedTask.id());
        assertEquals(originalTask.createdAt(), updatedTask.createdAt());
        assertEquals(originalTask.completed(), updatedTask.completed());
        assertEquals(originalTask.completedAt(), updatedTask.completedAt());
    }

    @Test
    void testUpdate_WithNullValues_ShouldKeepOriginalValues() {
        Task originalTask = Task.createNewWithDetails(
            "Original task",
            LocalDateTime.now().plusDays(1),
            "HIGH",
            "Original constraints",
            1L,
            null
        );

        Task updatedTask = originalTask.update(null, null, null, null);

        assertEquals(originalTask.description(), updatedTask.description());
        assertEquals(originalTask.deadline(), updatedTask.deadline());
        assertEquals(originalTask.priority(), updatedTask.priority());
        assertEquals(originalTask.constraints(), updatedTask.constraints());
    }

    @Test
    void testUpdateFromContext_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Task originalTask = Task.createNewWithDetails(
            "Original task",
            LocalDateTime.now().plusDays(1),
            "HIGH",
            "Original constraints",
            1L,
            objectMapper.createObjectNode()
        );

        ObjectNode context = objectMapper.createObjectNode()
            .put("description", "Updated via context")
            .put("priority", "MEDIUM");

        Task updatedTask = originalTask.updateFromContext(context);

        assertEquals("Updated via context", updatedTask.description());
        assertEquals("MEDIUM", updatedTask.priority());
        assertEquals(originalTask.deadline(), updatedTask.deadline());
        assertEquals(originalTask.constraints(), updatedTask.constraints());
        assertEquals(originalTask.parentId(), updatedTask.parentId());
        assertEquals(originalTask.metadata(), updatedTask.metadata());
        assertEquals(originalTask.completed(), updatedTask.completed());
        assertEquals(originalTask.createdAt(), updatedTask.createdAt());
        assertEquals(originalTask.completedAt(), updatedTask.completedAt());
    }

    @Test
    void testUpdateFromContext_WithInvalidData_ShouldThrowException() {
        Task originalTask = Task.createNew("Original task");
        ObjectNode context = objectMapper.createObjectNode()
            .put("priority", "INVALID");

        assertThrows(IllegalArgumentException.class, () ->
            originalTask.updateFromContext(context)
        );
    }
}