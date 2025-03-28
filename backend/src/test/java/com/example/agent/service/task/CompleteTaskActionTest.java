package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the CompleteTaskAction class.
 * Tests the task completion functionality and intent handling.
 */
class CompleteTaskActionTest {

    @Mock
    private TaskService taskService;

    private CompleteTaskAction completeTaskAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        completeTaskAction = new CompleteTaskAction(taskService);
    }

    @Test
    void execute_ShouldCompleteTask_WhenValidTaskIdProvided() {
        // Arrange
        Long taskId = 1L;
        Task originalTask = Task.createNew("Test task");
        Task completedTask = originalTask.markCompleted();
        when(taskService.completeTask(taskId)).thenReturn(completedTask);

        Map<String, Object> context = Map.of("taskId", String.valueOf(taskId));

        // Act
        Map<String, Object> result = completeTaskAction.execute(context);

        // Assert
        assertNotNull(result);
        assertEquals("Task completed successfully", result.get("message"));
        assertNotNull(result.get("task"));
        Task resultTask = (Task) result.get("task");
        assertTrue(resultTask.completed());
        assertNotNull(resultTask.completedAt());
    }

    @Test
    void execute_ShouldThrowException_WhenInvalidTaskIdProvided() {
        // Arrange
        Map<String, Object> context = Map.of("taskId", "invalid");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> completeTaskAction.execute(context));
    }

    @Test
    void execute_ShouldThrowException_WhenTaskIdMissing() {
        // Arrange
        Map<String, Object> context = Map.of("someOtherField", "value");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> completeTaskAction.execute(context));
    }

    @Test
    void canHandle_ShouldReturnTrue_WhenIntentIsCompleteTask() {
        // Act & Assert
        assertTrue(completeTaskAction.canHandle("COMPLETE_TASK"));
    }

    @Test
    void canHandle_ShouldReturnFalse_WhenIntentIsNotCompleteTask() {
        // Act & Assert
        assertFalse(completeTaskAction.canHandle("CREATE_TASK"));
        assertFalse(completeTaskAction.canHandle("LIST_TASKS"));
        assertFalse(completeTaskAction.canHandle(null));
    }
}