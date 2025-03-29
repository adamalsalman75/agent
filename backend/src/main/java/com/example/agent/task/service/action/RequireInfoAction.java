package com.example.agent.task.service.action;

public class RequireInfoAction implements TaskAction {
    @Override
    public TaskParameters execute(TaskParameters parameters) {
        return parameters;
    }

    @Override
    public boolean canHandle(String intent) {
        return "REQUIRE_INFO".equals(intent);
    }
}