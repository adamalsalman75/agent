package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the CreateTaskAction class.
 * Tests the task creation functionality and intent handling.
 */
class CreateTaskActionTest {

    @Mock
    private TaskService taskService;

    private CreateTaskAction createTaskAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        createTaskAction = new CreateTaskAction(taskService);
    }

    @Test
    void execute_ShouldCreateTask_WhenValidDescriptionProvided() {
        // Arrange
        String description = "Test task description";
        Task newTask = Task.createNew(description);
        when(taskService.createTask(any(Task.class))).thenReturn(newTask);

        Map<String, Object> context = Map.of("description", description);

        // Act
        Map<String, Object> result = createTaskAction.execute(context);

        // Assert
        assertNotNull(result);
        assertEquals("Task created successfully", result.get("message"));
        assertNotNull(result.get("task"));
        Task resultTask = (Task) result.get("task");
        assertEquals(description, resultTask.description());
        assertFalse(resultTask.completed());
        assertNull(resultTask.completedAt());
    }

    @Test
    void execute_ShouldThrowException_WhenDescriptionMissing() {
        // Arrange
        Map<String, Object> context = Map.of("someOtherField", "value");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> createTaskAction.execute(context));
    }

    @Test
    void canHandle_ShouldReturnTrue_WhenIntentIsCreateTask() {
        // Act & Assert
        assertTrue(createTaskAction.canHandle("CREATE_TASK"));
    }

    @Test
    void canHandle_ShouldReturnFalse_WhenIntentIsNotCreateTask() {
        // Act & Assert
        assertFalse(createTaskAction.canHandle("UPDATE_TASK"));
        assertFalse(createTaskAction.canHandle("DELETE_TASK"));
        assertFalse(createTaskAction.canHandle(null));
    }
}