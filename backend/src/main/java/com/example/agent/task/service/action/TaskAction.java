package com.example.agent.task.service.action;

public interface TaskAction {
    TaskParameters execute(TaskParameters parameters);
    boolean canHandle(String intent);
}