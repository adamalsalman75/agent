package com.example.agent.task.service;

import com.example.agent.common.model.Task;
import com.example.agent.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return StreamSupport.stream(taskRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findByCompletedFalse();
    }

    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        return taskRepository.save(task);
    }

    public Task completeTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        return taskRepository.save(task.markCompleted());
    }

    public List<Task> getSubtasks(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID cannot be null");
        }
        List<Task> subtasks = taskRepository.findSubtasks(parentId);
        if (subtasks.isEmpty()) {
            throw new IllegalArgumentException("No subtasks found for parent ID: " + parentId);
        }
        return subtasks;
    }

    public List<Task> getRootTasks() {
        return taskRepository.findRootTasks();
    }

    public List<Task> getTasksByPriority(String priority) {
        if (priority == null || !Set.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
            throw new IllegalArgumentException("Invalid priority value: " + priority);
        }
        return taskRepository.findByPriority(priority);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }

    public Task updateTask(Long taskId, Task updatedTask) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (updatedTask == null) {
            throw new IllegalArgumentException("Updated task cannot be null");
        }
        Task existingTask = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Task updated = existingTask.update(
            updatedTask.description(),
            updatedTask.deadline(),
            updatedTask.priority(),
            updatedTask.constraints()
        );
        return taskRepository.save(updated);
    }
}