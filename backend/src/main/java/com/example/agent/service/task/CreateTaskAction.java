package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateTaskAction implements TaskAction {
    private final TaskService taskService;

    public CreateTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String description = (String) context.get("description");
        Task task = Task.createNew(description);
        Task newTask = taskService.createTask(task);
        
        return Map.of(
            "message", "Task created successfully",
            "task", newTask
        );
    }

    @Override
    public boolean canHandle(String intent) {
        return "CREATE_TASK".equals(intent);
    }
}