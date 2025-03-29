package com.example.agent.ai.service.decision;

import com.example.agent.ai.model.ConversationContext;
import com.example.agent.common.model.TaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class Refinement {
    private static final Logger logger = LoggerFactory.getLogger(Refinement.class);

    // Record to define the structure expected from the LLM
    private record TaskRefinementResponse(
        String description,
        String deadline,
        String priority,
        String constraints,
        boolean needsMoreInfo,
        String followUpQuestion
    ) {}
    
    private final ChatClient chatClient;
    
    public Refinement(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    public ConversationContext refineTask(String query, TaskData previousData) {
        logger.info("Entering refineTask with query: {} and previousData: {}", query, previousData);

        String currentIntent = "CREATE_TASK";
        logger.debug("Default intent set to CREATE_TASK");

        String prompt = buildPrompt(query, previousData);
        logger.debug("Prompt built for LLM: {}", prompt);

        TaskRefinementResponse response = chatClient
            .prompt()
            .system("""
                You are a task refinement assistant. Analyze the task and help make it well-defined.
                Extract the following fields and format your response as JSON:
                - description: A clear task description
                - deadline: A deadline date in ISO format, or null if not specified
                - priority: priority level (HIGH, MEDIUM, LOW), or null if not specified
                - constraints: Any constraints or requirements, or null if not specified
                - needsMoreInfo: true if you need to ask a follow-up question, false otherwise
                - followUpQuestion: If needsMoreInfo is true, provide a specific question to ask
                """)
            .user(prompt)
            .call()
            .entity(TaskRefinementResponse.class);

        logger.debug("LLM response received: {}", response);

        TaskData collectedData = new TaskData(
            response.description(),
            response.deadline(),
            response.priority(),
            response.constraints(),
            null
        );

        logger.info("Collected data: {}", collectedData);

        ConversationContext context = new ConversationContext(
            currentIntent,
            collectedData,
            response.needsMoreInfo(),
            response.needsMoreInfo() ? response.followUpQuestion() : null,
            null
        );

        logger.info("Exiting refineTask with context: {}", context);
        return context;
    }
    
    private String buildPrompt(String query, TaskData previousData) {
        // If we don't have collected data yet, this is a new task
        if (previousData == null || (previousData.description() == null && previousData.taskId() == null)) {
            return """
                Analyze this task request and extract structured information:
                "%s"
                
                If anything is unclear, set needsMoreInfo to true and provide a specific followUpQuestion.
                """.formatted(query);
        } else {
            // Extract information from previous data for the prompt
            StringBuilder existingInfo = new StringBuilder("We already have the following information:\\n");
            
            if (previousData.description() != null) {
                existingInfo.append("- Description: ").append(previousData.description()).append("\\n");
            }
            
            if (previousData.deadline() != null) {
                existingInfo.append("- Deadline: ").append(previousData.deadline()).append("\\n");
            }
            
            if (previousData.priority() != null) {
                existingInfo.append("- Priority: ").append(previousData.priority()).append("\\n");
            }
            
            if (previousData.constraints() != null) {
                existingInfo.append("- Constraints: ").append(previousData.constraints()).append("\\n");
            }
            
            return """
                %s
                
                The user has provided a new response:
                "%s"
                
                Update the task information accordingly and provide the complete task details.
                """.formatted(existingInfo, query);
        }
    }
}