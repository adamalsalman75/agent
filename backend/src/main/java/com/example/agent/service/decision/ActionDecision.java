package com.example.agent.service.decision;

import com.example.agent.service.task.TaskAction;
import java.util.Map;

public record ActionDecision(TaskAction action, Map<String, Object> parameters) {}