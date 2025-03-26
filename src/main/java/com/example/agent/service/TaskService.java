package com.example.agent.service;

import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findByCompletedOrderByCreatedAtDesc(false);
    }

    public Task createTask(String description) {
        // Use the factory method from Task record
        Task newTask = Task.createNew(description);
        return taskRepository.save(newTask);
    }

    public Optional<Task> completeTask(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        
        if (taskOpt.isPresent()) {
            // Use the method from Task record to create a new completed version
            Task completedTask = taskOpt.get().markCompleted();
            return Optional.of(taskRepository.save(completedTask));
        }
        
        return Optional.empty();
    }
}