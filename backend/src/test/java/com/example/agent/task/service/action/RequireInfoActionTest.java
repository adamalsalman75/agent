package com.example.agent.task.service.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequireInfoActionTest {

    private RequireInfoAction action;

    @BeforeEach
    void setUp() {
        action = new RequireInfoAction();
    }

    @Test
    void execute_ReturnsUnmodifiedParameters() {
        // Setup
        TaskParameters params = TaskParameters.forCreateTask(
            "task-123",
            "Buy groceries",
            "2025-04-01",
            "high",
            "Must include vegetables"
        );

        // Execute
        TaskParameters result = action.execute(params);

        // Verify
        assertEquals(params, result);
    }

    @Test
    void canHandle_WithRequireInfoIntent_ReturnsTrue() {
        assertTrue(action.canHandle("REQUIRE_INFO"));
    }

    @Test
    void canHandle_WithOtherIntent_ReturnsFalse() {
        assertFalse(action.canHandle("CREATE_TASK"));
        assertFalse(action.canHandle("COMPLETE_TASK"));
        assertFalse(action.canHandle("LIST_TASKS"));
    }
}