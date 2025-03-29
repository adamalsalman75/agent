package com.example.agent.ai.service.decision;

import com.example.agent.task.service.action.TaskAction;
import com.example.agent.task.service.action.RequireInfoAction;
import com.example.agent.task.service.action.TaskParameters;
import com.example.agent.ai.model.ConversationContext;
import com.example.agent.common.model.TaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DecisionMaker {
    private static final Logger logger = LoggerFactory.getLogger(DecisionMaker.class);
    
    // Record to represent the intent classification response
    protected record IntentClassification(String intent) {}
    
    private final ChatClient chatClient;
    private final List<TaskAction> availableActions;
    private final Refinement refinement;
    
    public DecisionMaker(ChatClient chatClient, List<TaskAction> taskActions, Refinement refinement) {
        this.chatClient = chatClient;
        this.availableActions = taskActions;
        this.refinement = refinement;
    }
    
    public Optional<ActionDecision> decide(Map<String, Object> context) {
        logger.info("Entering decide with context: {}", context);

        String query = (String) context.get("query");
        if (query == null) {
            logger.warn("Query is null in context");
            return Optional.empty();
        }

        IntentClassification intentClassification = classifyIntent(query);
        String intent = intentClassification.intent();
        logger.debug("Intent classified as: {}", intent);

        if (intent == null || intent.isEmpty()) {
            logger.warn("No intent recognized for query: {}", query);
            return Optional.empty();
        }

        // Get previous context if available
        ConversationContext previousContext = (ConversationContext) context.get("previousContext");
        TaskData previousData = previousContext != null ? previousContext.collectedData() : null;

        logger.info("Refining task with query: {} and previousData: {}", query, previousData);
        ConversationContext refinementContext = refinement.refineTask(query, previousData);

        if (refinementContext.requiresFollowUp()) {
            logger.info("Follow-up required: {}", refinementContext.nextPrompt());
            return Optional.of(ActionDecision.requireMoreInfo(
                new RequireInfoAction(),
                refinementContext.nextPrompt(),
                refinementContext
            ));
        }

        // Only proceed with action creation if we have all needed information
        TaskParameters parameters = buildParameters(intent, refinementContext.collectedData());
        logger.debug("Task parameters built: {}", parameters);

        Optional<ActionDecision> actionDecision = availableActions.stream()
                .filter(action -> action.canHandle(intent))
                .findFirst()
                .map(action -> new ActionDecision(action, parameters));

        logger.info("Exiting decide with actionDecision: {}", actionDecision);
        return actionDecision;
    }

    private IntentClassification classifyIntent(String query) {
        return chatClient
                .prompt()
                .system("""
                    You are a task management assistant that helps users manage their tasks.
                    Analyze the user's intent and respond with one of these values for the 'intent' field:
                    - CREATE_TASK
                    - COMPLETE_TASK
                    - LIST_TASKS
                    
                    Format your response as valid JSON with a single 'intent' field.
                    """)
                .user(query)
                .call()
                .entity(IntentClassification.class);
    }

    private TaskParameters buildParameters(String intent, TaskData taskData) {
        if (taskData == null) {
            return null;
        }
        
        return switch (intent) {
            case "CREATE_TASK" -> TaskParameters.forCreateTask(
                null, // taskId is null for new tasks
                taskData.description() != null ? taskData.description() : "",
                taskData.deadline(),
                taskData.priority(),
                taskData.constraints()
            );
            case "COMPLETE_TASK" -> TaskParameters.forCompleteTask(
                taskData.taskId() != null ? taskData.taskId() : ""
            );
            default -> null;
        };
    }
}
