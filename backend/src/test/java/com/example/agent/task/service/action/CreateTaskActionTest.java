package com.example.agent.task.service.action;

import com.example.agent.common.model.Task;
import com.example.agent.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTaskActionTest {

    @Mock
    private TaskService taskService;

    private CreateTaskAction action;

    @BeforeEach
    void setUp() {
        action = new CreateTaskAction(taskService);
    }

    @Test
    void execute_WithValidParameters_CreatesTask() {
        // Setup
        TaskParameters params = TaskParameters.forCreateTask(
            null,
            "Buy groceries",
            "2025-04-01T10:00:00",
            "HIGH",
            "Must include vegetables"
        );

        Task createdTask = Task.createForTesting(
            "Buy groceries",
            java.time.LocalDateTime.parse("2025-04-01T10:00:00"),
            "HIGH",
            "Must include vegetables",
            null,
            null
        );

        when(taskService.createTask(any())).thenReturn(createdTask);

        // Execute
        TaskParameters result = action.execute(params);

        // Verify
        assertNotNull(result);
        assertEquals("Buy groceries", result.description());
        assertEquals("2025-04-01T10:00:00", result.deadline());
        assertEquals("HIGH", result.priority());
        assertEquals("Must include vegetables", result.constraints());
        verify(taskService).createTask(any());
    }

    @Test
    void execute_WithNullParameters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> action.execute(null));
    }

    @Test
    void execute_WithEmptyDescription_ThrowsException() {
        TaskParameters params = TaskParameters.forCreateTask(
            null,
            "",
            null,
            null,
            null
        );

        assertThrows(IllegalArgumentException.class, () -> action.execute(params));
    }

    @Test
    void canHandle_WithCreateTaskIntent_ReturnsTrue() {
        assertTrue(action.canHandle("CREATE_TASK"));
    }

    @Test
    void canHandle_WithOtherIntent_ReturnsFalse() {
        assertFalse(action.canHandle("COMPLETE_TASK"));
        assertFalse(action.canHandle("LIST_TASKS"));
        assertFalse(action.canHandle("UNKNOWN"));
    }
}