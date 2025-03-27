package com.example.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

public record QueryRequest(
    String query,
    JsonNode context
) {}