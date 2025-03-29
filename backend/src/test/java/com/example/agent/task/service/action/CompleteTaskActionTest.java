package com.example.agent.task.service.action;

import com.example.agent.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompleteTaskActionTest {

    @Mock
    private TaskService taskService;

    private CompleteTaskAction action;

    @BeforeEach
    void setUp() {
        action = new CompleteTaskAction(taskService);
    }

    @Test
    void execute_WithValidTaskId_CompletesTask() {
        // Setup
        TaskParameters params = TaskParameters.forCompleteTask("123");

        // Execute
        TaskParameters result = action.execute(params);

        // Verify
        assertNotNull(result);
        assertEquals("123", result.taskId());
        verify(taskService).completeTask(123L);
    }

    @Test
    void execute_WithInvalidTaskId_ThrowsException() {
        // Setup
        TaskParameters params = TaskParameters.forCompleteTask("invalid");

        // Execute & Verify
        assertThrows(NumberFormatException.class, () -> action.execute(params));
    }

    @Test
    void canHandle_WithCompleteTaskIntent_ReturnsTrue() {
        assertTrue(action.canHandle("COMPLETE_TASK"));
    }

    @Test
    void canHandle_WithOtherIntent_ReturnsFalse() {
        assertFalse(action.canHandle("CREATE_TASK"));
        assertFalse(action.canHandle("LIST_TASKS"));
        assertFalse(action.canHandle("UNKNOWN"));
    }
}