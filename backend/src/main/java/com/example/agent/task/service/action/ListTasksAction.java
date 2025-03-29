package com.example.agent.task.service.action;

import com.example.agent.common.model.Task;
import com.example.agent.task.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ListTasksAction is responsible for listing tasks in the system.
 * This class implements the TaskAction interface and specifically handles the LIST_TASKS intent.
 * It works with the TaskService to retrieve tasks from the system.
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
     * Executes the list tasks action by processing the provided parameters.
     * @param parameters The parameters for finding tasks (may include filters)
     * @return The task parameters with updated information
     */
    @Override
    public TaskParameters execute(TaskParameters parameters) {
        List<Task> tasks = taskService.getAllTasks();
        
        // For now, just return the parameters unchanged
        // In a more advanced implementation, you might include task listing information
        return parameters;
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