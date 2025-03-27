package com.example.agent.service.task;

import java.util.Map;

public interface TaskAction {
    Map<String, Object> execute(Map<String, Object> context);
    boolean canHandle(String intent);
}