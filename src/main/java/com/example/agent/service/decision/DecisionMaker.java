package com.example.agent.service.decision;

import com.example.agent.service.task.TaskAction;
import java.util.Map;
import java.util.Optional;

public interface DecisionMaker {
    /**
     * Evaluates the current state and decides what action to take
     * @param context Current state and any relevant information
     * @param knowledgeBase Access to stored knowledge
     * @return The chosen task action and its parameters
     */
    Optional<ActionDecision> decide(Map<String, Object> context);
}

record ActionDecision(TaskAction action, Map<String, Object> parameters) {}