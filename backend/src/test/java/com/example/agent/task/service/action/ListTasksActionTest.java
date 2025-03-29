package com.example.agent.task.service.action;

import com.example.agent.common.model.Task;
import com.example.agent.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListTasksActionTest {

    @Mock
    private TaskService taskService;

    private ListTasksAction listTasksAction;

    @BeforeEach
    void setUp() {
        listTasksAction = new ListTasksAction(taskService);
    }

    @Test
    void execute_ShouldReturnParameters() {
        // Setup
        TaskParameters parameters = TaskParameters.forCreateTask(
            null, 
            "Test task", 
            null, 
            "MEDIUM", 
            null
        );
        
        List<Task> mockTasks = Arrays.asList(
            Task.createNewWithDetails("Task 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("Task 2", LocalDateTime.now().plusDays(1), "MEDIUM", null, null, null)
        );
        
        when(taskService.getAllTasks()).thenReturn(mockTasks);
        
        // Execute
        TaskParameters result = listTasksAction.execute(parameters);
        
        // Verify
        assertNotNull(result);
        assertEquals(parameters, result);
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void canHandle_WithListTasksIntent_ShouldReturnTrue() {
        // Execute & Verify
        assertTrue(listTasksAction.canHandle("LIST_TASKS"));
    }

    @Test
    void canHandle_WithOtherIntents_ShouldReturnFalse() {
        // Execute & Verify
        assertFalse(listTasksAction.canHandle("CREATE_TASK"));
        assertFalse(listTasksAction.canHandle("COMPLETE_TASK"));
        assertFalse(listTasksAction.canHandle("UNKNOWN_INTENT"));
    }
}