package com.example.agent.service.reasoning;

import java.util.List;
import java.util.Map;

public interface Refinement {
    /**
     * Iteratively refines an initial response through user feedback
     * @param initialInput The starting query or request
     * @param refinementContext Additional context including user feedback
     * @return The refined result and any follow-up questions
     */
    RefinementResult refine(String initialInput, Map<String, Object> refinementContext);
}

record RefinementResult(
    Map<String, Object> refinedOutput,
    List<String> followUpQuestions,
    boolean needsMoreFeedback
) {}