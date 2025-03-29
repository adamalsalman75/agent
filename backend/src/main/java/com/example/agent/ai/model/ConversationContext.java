package com.example.agent.ai.model;

import com.example.agent.common.model.Task;
import com.example.agent.common.model.TaskData;

public record ConversationContext(
    String currentIntent,
    TaskData collectedData,
    boolean requiresFollowUp,
    String nextPrompt,
    Task inProgressTask
) {
    public static ConversationContext createNew() {
        return new ConversationContext(null, TaskData.createEmpty(), false, null, null);
    }
    
    public String getCurrentIntent() {
        return currentIntent;
    }
    
    public TaskData getCollectedData() {
        return collectedData;
    }
    
    public String getNextPrompt() {
        return nextPrompt;
    }
    
    public Task getInProgressTask() {
        return inProgressTask;
    }
}