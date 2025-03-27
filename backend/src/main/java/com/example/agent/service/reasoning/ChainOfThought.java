package com.example.agent.service.reasoning;

import java.util.List;
import java.util.Map;

public interface ChainOfThought {
    /**
     * Breaks down complex problems into steps and shows the reasoning process
     * @param input The initial problem or query
     * @return The steps of reasoning and final conclusion
     */
    ReasoningResult reason(String input);
}

record ReasoningResult(
    List<String> reasoningSteps,
    Map<String, Object> conclusion
) {}