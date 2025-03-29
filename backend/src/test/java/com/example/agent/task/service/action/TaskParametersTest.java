package com.example.agent.task.service.action;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskParametersTest {

    @Test
    void forCreateTask_WithAllParameters_CreatesValidParameters() {
        // Execute
        TaskParameters params = TaskParameters.forCreateTask(
            "task-123",
            "Buy groceries",
            "2025-04-01",
            "high",
            "Must include vegetables"
        );

        // Verify
        assertEquals("task-123", params.taskId());
        assertEquals("Buy groceries", params.description());
        assertEquals("2025-04-01", params.deadline());
        assertEquals("high", params.priority());
        assertEquals("Must include vegetables", params.constraints());
    }

    @Test
    void forCreateTask_WithNullOptionalParameters_CreatesValidParameters() {
        // Execute
        TaskParameters params = TaskParameters.forCreateTask(
            "task-123",
            "Buy groceries",
            null,
            null,
            null
        );

        // Verify
        assertEquals("task-123", params.taskId());
        assertEquals("Buy groceries", params.description());
        assertNull(params.deadline());
        assertNull(params.priority());
        assertNull(params.constraints());
    }

    @Test
    void forCompleteTask_CreatesParametersWithOnlyTaskId() {
        // Execute
        TaskParameters params = TaskParameters.forCompleteTask("task-123");

        // Verify
        assertEquals("task-123", params.taskId());
        assertNull(params.description());
        assertNull(params.deadline());
        assertNull(params.priority());
        assertNull(params.constraints());
    }

    @Test
    void forCreateTask_WithEmptyOptionalParameters_CreatesValidParameters() {
        // Execute
        TaskParameters params = TaskParameters.forCreateTask(
            "task-123",
            "Buy groceries",
            "",
            "",
            ""
        );

        // Verify
        assertEquals("task-123", params.taskId());
        assertEquals("Buy groceries", params.description());
        assertEquals("", params.deadline());
        assertEquals("", params.priority());
        assertEquals("", params.constraints());
    }
}