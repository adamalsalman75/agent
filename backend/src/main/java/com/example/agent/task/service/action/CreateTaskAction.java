package com.example.agent.task.service.action;

import com.example.agent.common.model.Task;
import com.example.agent.task.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateTaskAction implements TaskAction {
    private static final Logger logger = LoggerFactory.getLogger(CreateTaskAction.class);

    private final TaskService taskService;

    public CreateTaskAction(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public TaskParameters execute(TaskParameters parameters) {
        logger.info("Entering execute method with parameters: {}", parameters);

        if (parameters == null || parameters.description() == null || parameters.description().isEmpty()) {
            logger.warn("Invalid task parameters: {}", parameters);
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        try {
            logger.debug("Creating new task with description: {}", parameters.description());
            Task task = Task.createNewWithDetails(
                parameters.description(),
                parameters.deadline() != null ? java.time.LocalDateTime.parse(parameters.deadline()) : null,
                parameters.priority(),
                parameters.constraints(),
                null,
                null
            );
            
            logger.debug("Saving task to repository");
            task = taskService.createTask(task);
            logger.info("Task created successfully with ID: {}", task.id());
            
            // Return new parameters with the task ID if available
            return TaskParameters.forCreateTask(
                task.id() != null ? task.id().toString() : null,
                task.description(),
                task.deadline() != null ? task.deadline().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                task.priority(),
                task.constraints()
            );
        } catch (Exception e) {
            logger.error("Error occurred while creating task: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("Exiting execute method");
        }
    }

    @Override
    public boolean canHandle(String intent) {
        return "CREATE_TASK".equals(intent);
    }
}