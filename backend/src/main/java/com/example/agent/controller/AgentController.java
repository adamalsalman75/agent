package com.example.agent.controller;

import com.example.agent.model.QueryRequest;
import com.example.agent.model.QueryResponse;
import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import com.example.agent.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // For development
public class AgentController {
    private final AIService aiService;
    private final TaskRepository taskRepository;

    public AgentController(AIService aiService, TaskRepository taskRepository) {
        this.aiService = aiService;
        this.taskRepository = taskRepository;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> processQuery(@RequestBody QueryRequest request) {
        return ResponseEntity.ok(aiService.processQuery(request));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok((List<Task>) taskRepository.findAll());
    }

    @GetMapping("/tasks/active")
    public ResponseEntity<List<Task>> getActiveTasks() {
        return ResponseEntity.ok(taskRepository.findByCompletedFalse());
    }

    @GetMapping("/tasks/{id}/subtasks")
    public ResponseEntity<List<Task>> getSubtasks(@PathVariable Long id) {
        return ResponseEntity.ok(taskRepository.findSubtasks(id));
    }

    @GetMapping("/tasks/root")
    public ResponseEntity<List<Task>> getRootTasks() {
        return ResponseEntity.ok(taskRepository.findRootTasks());
    }

    @GetMapping("/tasks/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(taskRepository.findByPriority(priority));
    }

    @GetMapping("/tasks/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        return ResponseEntity.ok(taskRepository.findOverdueTasks());
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        return ResponseEntity.ok(taskRepository.save(task.markCompleted()));
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskRepository.save(task.update(
            task.description(),
            task.deadline(),
            task.priority(),
            task.constraints()
        )));
    }
}