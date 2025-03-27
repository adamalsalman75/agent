package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CompleteTaskAction implements TaskAction {
    private final TaskService taskService;

    public CompleteTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        Long taskId = Long.parseLong((String) context.get("taskId"));
        Optional<Task> completedTask = taskService.completeTask(taskId);
        
        if (completedTask.isPresent()) {
            return Map.of(
                "message", "Task completed successfully",
                "task", completedTask.get()
            );
        }
        
        throw new RuntimeException("Task not found with id: " + taskId);
    }

    @Override
    public boolean canHandle(String intent) {
        return "COMPLETE_TASK".equals(intent);
    }
}