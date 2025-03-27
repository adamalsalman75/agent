package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CompleteTaskAction implements TaskAction {
    private final TaskService taskService;

    public CompleteTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        Long taskId = Long.parseLong((String) context.get("taskId"));
        Task completedTask = taskService.completeTask(taskId);
        
        return Map.of(
            "message", "Task completed successfully",
            "task", completedTask
        );
    }

    @Override
    public boolean canHandle(String intent) {
        return "COMPLETE_TASK".equals(intent);
    }
}