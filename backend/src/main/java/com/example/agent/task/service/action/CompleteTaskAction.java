package com.example.agent.task.service.action;

import com.example.agent.task.service.TaskService;
import org.springframework.stereotype.Component;

/**
 * CompleteTaskAction is responsible for handling the completion of tasks in the system.
 * This class implements the TaskAction interface and specifically handles the COMPLETE_TASK intent.
 */
@Component
public class CompleteTaskAction implements TaskAction {
    private final TaskService taskService;

    public CompleteTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Executes the task completion action by processing the provided parameters.
     * @param parameters The task parameters containing the taskId
     * @return The task parameters with updated information
     */
    @Override
    public TaskParameters execute(TaskParameters parameters) {
        String taskId = parameters.taskId();
        taskService.completeTask(Long.parseLong(taskId));
        
        // Return the parameters with additional information if needed
        return parameters;
    }

    /**
     * Determines if this action can handle the given intent.
     * @param intent The intent to check
     * @return true if the intent is "COMPLETE_TASK", false otherwise
     */
    @Override
    public boolean canHandle(String intent) {
        return "COMPLETE_TASK".equals(intent);
    }
}