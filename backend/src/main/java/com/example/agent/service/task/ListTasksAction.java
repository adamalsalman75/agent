package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ListTasksAction is responsible for retrieving and returning all tasks in the system.
 * This class implements the TaskAction interface and specifically handles the LIST_TASKS intent.
 * It works with the TaskService to fetch all existing tasks from the persistence layer.
 */
@Component
public class ListTasksAction implements TaskAction {
    private final TaskService taskService;

    /**
     * Constructs a new ListTasksAction with the required TaskService dependency.
     * @param taskService The service responsible for task retrieval operations
     */
    public ListTasksAction(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Executes the task listing action by retrieving all tasks from the system.
     * @param context A map containing any context parameters (not used in this implementation)
     * @return A map containing the list result with "message" and "tasks" keys
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        List<Task> tasks = taskService.getAllTasks();
        return Map.of(
            "message", "Here are your tasks",
            "tasks", tasks
        );
    }

    /**
     * Determines if this action can handle the given intent.
     * @param intent The intent to check
     * @return true if the intent is "LIST_TASKS", false otherwise
     */
    @Override
    public boolean canHandle(String intent) {
        return "LIST_TASKS".equals(intent);
    }
}