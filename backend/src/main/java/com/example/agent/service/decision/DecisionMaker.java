package com.example.agent.service.decision;

import java.util.Map;
import java.util.Optional;

public interface DecisionMaker {
    /**
     * Evaluates the current state and decides what action to take
     * @param context Current state and any relevant information
     * @return The chosen task action and its parameters
     */
    Optional<ActionDecision> decide(Map<String, Object> context);
}