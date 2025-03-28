package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ListTasksAction class.
 * Tests the task listing functionality and intent handling.
 */
class ListTasksActionTest {

    @Mock
    private TaskService taskService;

    private ListTasksAction listTasksAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listTasksAction = new ListTasksAction(taskService);
    }

    @Test
    void execute_ShouldReturnTasks_WhenTasksExist() {
        // Arrange
        List<Task> tasks = List.of(
            Task.createNew("Task 1"),
            Task.createNew("Task 2")
        );
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act
        Map<String, Object> result = listTasksAction.execute(Map.of());

        // Assert
        assertNotNull(result);
        assertEquals("Here are your tasks", result.get("message"));
        assertNotNull(result.get("tasks"));
        @SuppressWarnings("unchecked")
        List<Task> resultTasks = (List<Task>) result.get("tasks");
        assertEquals(2, resultTasks.size());
        assertEquals("Task 1", resultTasks.get(0).description());
        assertEquals("Task 2", resultTasks.get(1).description());
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoTasksExist() {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(List.of());

        // Act
        Map<String, Object> result = listTasksAction.execute(Map.of());

        // Assert
        assertNotNull(result);
        assertEquals("Here are your tasks", result.get("message"));
        assertNotNull(result.get("tasks"));
        @SuppressWarnings("unchecked")
        List<Task> resultTasks = (List<Task>) result.get("tasks");
        assertTrue(resultTasks.isEmpty());
    }

    @Test
    void canHandle_ShouldReturnTrue_WhenIntentIsListTasks() {
        // Act & Assert
        assertTrue(listTasksAction.canHandle("LIST_TASKS"));
    }

    @Test
    void canHandle_ShouldReturnFalse_WhenIntentIsNotListTasks() {
        // Act & Assert
        assertFalse(listTasksAction.canHandle("CREATE_TASK"));
        assertFalse(listTasksAction.canHandle("COMPLETE_TASK"));
        assertFalse(listTasksAction.canHandle(null));
    }
}