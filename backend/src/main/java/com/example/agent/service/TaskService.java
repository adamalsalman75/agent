package com.example.agent.service;

import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        taskRepository.findAll().forEach(tasks::add);
        return tasks;
    }
    
    public List<Task> getActiveTasks() {
        return taskRepository.findByCompletedFalse();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task completeTask(Long id) {
        return taskRepository.findById(id)
            .map(Task::markCompleted)
            .map(taskRepository::save)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public List<Task> getSubtasks(Long parentId) {
        return taskRepository.findSubtasks(parentId);
    }

    public List<Task> getRootTasks() {
        return taskRepository.findRootTasks();
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }
}