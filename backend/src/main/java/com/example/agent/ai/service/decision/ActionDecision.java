package com.example.agent.ai.service.decision;

import com.example.agent.task.service.action.TaskAction;
import com.example.agent.task.service.action.TaskParameters;

public record ActionDecision(
    TaskAction action, 
    TaskParameters parameters,
    String nextPrompt,
    Object context
) {
    /**
     * Constructor for creating an action decision with just an action and parameters
     */
    public ActionDecision(TaskAction action, TaskParameters parameters) {
        this(action, parameters, null, null);
    }
    
    /**
     * Constructor for follow-up requests that need additional information
     */
    public static ActionDecision requireMoreInfo(
            TaskAction action, 
            String nextPrompt, 
            Object context) {
        return new ActionDecision(action, null, nextPrompt, context);
    }
}