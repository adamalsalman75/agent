package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListTasksAction implements TaskAction {
    private final TaskService taskService;

    public ListTasksAction(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        List<Task> tasks = taskService.getAllTasks();
        return Map.of(
            "message", "Here are your tasks",
            "tasks", tasks
        );
    }

    @Override
    public boolean canHandle(String intent) {
        return "LIST_TASKS".equals(intent);
    }
}