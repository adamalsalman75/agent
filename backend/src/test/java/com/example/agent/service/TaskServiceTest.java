package com.example.agent.service;

import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        Task task1 = Task.createNew("Task 1");
        Task task2 = Task.createNew("Task 2");
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        verify(taskRepository).findAll();
    }

    @Test
    void getActiveTasks_ShouldReturnUncompletedTasks() {
        Task task = Task.createNew("Active task");
        when(taskRepository.findByCompletedFalse()).thenReturn(List.of(task));

        List<Task> activeTasks = taskService.getActiveTasks();

        assertEquals(1, activeTasks.size());
        assertFalse(activeTasks.get(0).completed());
        verify(taskRepository).findByCompletedFalse();
    }

    @Test
    void createTask_ShouldSaveAndReturnTask() {
        LocalDateTime now = LocalDateTime.now();
        Task newTask = Task.createNewWithDetails(
            "New task",
            now.plusDays(1),
            "HIGH",
            "Test constraints",
            null,
            null
        );
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        Task createdTask = taskService.createTask(newTask);

        assertNotNull(createdTask);
        assertEquals(newTask.description(), createdTask.description());
        assertEquals(newTask.deadline(), createdTask.deadline());
        assertEquals(newTask.priority(), createdTask.priority());
        assertEquals(newTask.constraints(), createdTask.constraints());
        assertFalse(createdTask.completed());
        assertNull(createdTask.completedAt());
        verify(taskRepository).save(newTask);
    }

    @Test
    void completeTask_ShouldMarkTaskAsCompleted() {
        Task task = Task.createNew("Task to complete");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task completedTask = taskService.completeTask(1L);

        assertTrue(completedTask.completed());
        assertNotNull(completedTask.completedAt());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void completeTask_WhenTaskNotFound_ShouldThrowException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.completeTask(1L));
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_WhenTaskIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(null));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getSubtasks_ShouldReturnChildTasks() {
        Task subtask = Task.createNewWithDetails(
            "Subtask",
            null,
            null,
            null,
            1L,
            null
        );
        when(taskRepository.findSubtasks(1L)).thenReturn(List.of(subtask));

        List<Task> subtasks = taskService.getSubtasks(1L);

        assertEquals(1, subtasks.size());
        assertEquals(1L, subtasks.get(0).parentId());
        verify(taskRepository).findSubtasks(1L);
    }

    @Test
    void getRootTasks_ShouldReturnTopLevelTasks() {
        Task rootTask = Task.createNew("Root task");
        when(taskRepository.findRootTasks()).thenReturn(List.of(rootTask));

        List<Task> rootTasks = taskService.getRootTasks();

        assertEquals(1, rootTasks.size());
        assertNull(rootTasks.get(0).parentId());
        verify(taskRepository).findRootTasks();
    }

    @Test
    void getTasksByPriority_ShouldReturnTasksWithSpecifiedPriority() {
        Task highPriorityTask = Task.createNewWithDetails(
            "High priority task",
            null,
            "HIGH",
            null,
            null,
            null
        );
        when(taskRepository.findByPriority("HIGH")).thenReturn(List.of(highPriorityTask));

        List<Task> tasks = taskService.getTasksByPriority("HIGH");

        assertEquals(1, tasks.size());
        assertEquals("HIGH", tasks.get(0).priority());
        verify(taskRepository).findByPriority("HIGH");
    }

    @Test
    void getTasksByPriority_WhenInvalidPriority_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> taskService.getTasksByPriority("INVALID"));
        verify(taskRepository, never()).findByPriority(any());
    }

    @Test
    void getOverdueTasks_ShouldReturnUncompletedTasksPastDeadline() {
        Task overdueTask = Task.createForTesting(
            "Overdue task",
            LocalDateTime.now().minusDays(1),
            null,
            null,
            null,
            null
        );
        when(taskRepository.findOverdueTasks()).thenReturn(List.of(overdueTask));

        List<Task> overdueTasks = taskService.getOverdueTasks();

        assertEquals(1, overdueTasks.size());
        assertTrue(overdueTasks.get(0).deadline().isBefore(LocalDateTime.now()));
        verify(taskRepository).findOverdueTasks();
    }

    @Test
    void updateTask_ShouldUpdateAndReturnTask() {
        Task existingTask = Task.createNew("Original task");
        Task updatedTaskDetails = Task.createForTesting(
            "Updated task",
            LocalDateTime.now(),
            "HIGH",
            "New constraints",
            null,
            null
        );
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(1L, updatedTaskDetails);

        assertEquals(updatedTaskDetails.description(), result.description());
        assertEquals(updatedTaskDetails.deadline(), result.deadline());
        assertEquals(updatedTaskDetails.priority(), result.priority());
        assertEquals(updatedTaskDetails.constraints(), result.constraints());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskNotFound_ShouldThrowException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        Task updatedTask = Task.createNew("Updated task");

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(1L, updatedTask));
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }
}