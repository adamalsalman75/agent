package com.example.agent.service;

import com.example.agent.service.decision.DecisionMaker;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AIService {
    private final DecisionMaker decisionMaker;

    public AIService(DecisionMaker decisionMaker) {
        this.decisionMaker = decisionMaker;
    }

    public Map<String, Object> processQuery(String query) {
        return decisionMaker.decide(Map.of("query", query))
                .map(decision -> decision.action().execute(decision.parameters()))
                .orElseThrow(() -> new RuntimeException("Could not determine action for query: " + query));
    }
}