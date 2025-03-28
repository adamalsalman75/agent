package com.example.agent.service.task;

import com.example.agent.model.Task;
import com.example.agent.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CreateTaskAction is responsible for handling the creation of new tasks in the system.
 * This class implements the TaskAction interface and specifically handles the CREATE_TASK intent.
 * It works with the TaskService to persist new tasks in the system.
 */
@Component
public class CreateTaskAction implements TaskAction {
    private final TaskService taskService;

    /**
     * Constructs a new CreateTaskAction with the required TaskService dependency.
     * @param taskService The service responsible for task persistence operations
     */
    public CreateTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Executes the task creation action by processing the provided context.
     * @param context A map containing the task details, must include a "description" key
     * @return A map containing the creation result with "message" and "task" keys
     */
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

    /**
     * Determines if this action can handle the given intent.
     * @param intent The intent to check
     * @return true if the intent is "CREATE_TASK", false otherwise
     */
    @Override
    public boolean canHandle(String intent) {
        return "CREATE_TASK".equals(intent);
    }
}