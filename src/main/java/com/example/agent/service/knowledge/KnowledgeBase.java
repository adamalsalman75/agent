package com.example.agent.service.knowledge;

import java.util.Map;
import java.util.Optional;

public interface KnowledgeBase {
    void store(String key, Object value);
    Optional<Object> retrieve(String key);
    void update(String key, Object value);
    Map<String, Object> getAll();
    void clear();
}