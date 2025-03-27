package com.example.agent.controller;

import com.example.agent.model.Task;
import com.example.agent.service.AIService;
import com.example.agent.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AgentController {

    private final AIService aiService;
    private final TaskService taskService;

    @Autowired
    public AgentController(AIService aiService, TaskService taskService) {
        this.aiService = aiService;
        this.taskService = taskService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> processQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query cannot be empty");
        }

        try {
            Map<String, Object> response = aiService.processQuery(query);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/tasks/active")
    public ResponseEntity<List<Task>> getActiveTasks() {
        return ResponseEntity.ok(taskService.getActiveTasks());
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Task task = taskService.createTask(description);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        Optional<Task> task = taskService.completeTask(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}