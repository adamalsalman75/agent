package com.example.agent.ai.controller;

import com.example.agent.ai.model.QueryRequest;
import com.example.agent.ai.model.QueryResponse;
import com.example.agent.ai.service.AIService;
import com.example.agent.common.model.Task;
import com.example.agent.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AIService aiService;

    @Mock
    private TaskRepository taskRepository;

    private AgentController agentController;

    @BeforeEach
    void setUp() {
        agentController = new AgentController(aiService, taskRepository);
    }

    @Test
    void processQuery_ShouldReturnResponseFromAIService() {
        // Setup
        QueryRequest request = new QueryRequest("Create a task", null);
        QueryResponse expectedResponse = new QueryResponse(
            "Task processed successfully",
            Task.createNewWithDetails("Test task", null, "MEDIUM", null, null, null),
            false,
            null
        );

        when(aiService.processQuery(request)).thenReturn(expectedResponse);

        // Execute
        ResponseEntity<QueryResponse> response = agentController.processQuery(request);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Setup
        List<Task> expectedTasks = Arrays.asList(
            Task.createNewWithDetails("Task 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("Task 2", LocalDateTime.now().plusDays(1), "MEDIUM", null, null, null)
        );

        when(taskRepository.findAll()).thenReturn(expectedTasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getAllTasks();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedTasks, response.getBody());
    }

    @Test
    void getActiveTasks_ShouldReturnActiveTasks() {
        // Setup
        List<Task> expectedTasks = Arrays.asList(
            Task.createNewWithDetails("Task 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("Task 2", LocalDateTime.now().plusDays(1), "MEDIUM", null, null, null)
        );

        when(taskRepository.findByCompletedFalse()).thenReturn(expectedTasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getActiveTasks();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedTasks, response.getBody());
    }

    @Test
    void getSubtasks_ShouldReturnSubtasks() {
        // Setup
        Long parentId = 1L;
        List<Task> expectedSubtasks = Arrays.asList(
            Task.createNewWithDetails("Subtask 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("Subtask 2", null, "MEDIUM", null, null, null)
        );

        when(taskRepository.findSubtasks(parentId)).thenReturn(expectedSubtasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getSubtasks(parentId);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedSubtasks, response.getBody());
    }

    @Test
    void getRootTasks_ShouldReturnRootTasks() {
        // Setup
        List<Task> expectedTasks = Arrays.asList(
            Task.createNewWithDetails("Root Task 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("Root Task 2", null, "MEDIUM", null, null, null)
        );

        when(taskRepository.findRootTasks()).thenReturn(expectedTasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getRootTasks();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedTasks, response.getBody());
    }

    @Test
    void getTasksByPriority_ShouldReturnTasksWithSpecifiedPriority() {
        // Setup
        String priority = "HIGH";
        List<Task> expectedTasks = Arrays.asList(
            Task.createNewWithDetails("High Priority Task 1", null, "HIGH", null, null, null),
            Task.createNewWithDetails("High Priority Task 2", null, "HIGH", null, null, null)
        );

        when(taskRepository.findByPriority(priority)).thenReturn(expectedTasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getTasksByPriority(priority);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedTasks, response.getBody());
    }

    @Test
    void getOverdueTasks_ShouldReturnOverdueTasks() {
        // Setup
        // Create tasks with future dates, but mock them as overdue tasks
        List<Task> expectedTasks = Arrays.asList(
            Task.createNewWithDetails("Overdue Task 1", LocalDateTime.now().plusDays(1), "HIGH", null, null, null),
            Task.createNewWithDetails("Overdue Task 2", LocalDateTime.now().plusDays(2), "MEDIUM", null, null, null)
        );

        when(taskRepository.findOverdueTasks()).thenReturn(expectedTasks);

        // Execute
        ResponseEntity<List<Task>> response = agentController.getOverdueTasks();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedTasks, response.getBody());
    }

    @Test
    void completeTask_ShouldReturnCompletedTask() {
        // Setup
        Long taskId = 1L;
        Task task = Task.createNewWithDetails("Task to complete", null, "MEDIUM", null, null, null);
        Task completedTask = task.markCompleted();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

        // Execute
        ResponseEntity<Task> response = agentController.completeTask(taskId);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(completedTask, response.getBody());
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        // Setup
        Task taskToCreate = Task.createNewWithDetails("New task", null, "MEDIUM", null, null, null);

        when(taskRepository.save(taskToCreate)).thenReturn(taskToCreate);

        // Execute
        ResponseEntity<Task> response = agentController.createTask(taskToCreate);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(taskToCreate, response.getBody());
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() {
        // Setup
        Long taskId = 1L;
        Task taskToUpdate = Task.createNewWithDetails("Updated task", null, "HIGH", null, null, null);
        Task updatedTask = Task.createNewWithDetails("Updated task", null, "HIGH", null, null, null);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Execute
        ResponseEntity<Task> response = agentController.updateTask(taskId, taskToUpdate);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedTask, response.getBody());
    }
}
