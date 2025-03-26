package com.example.agent.service.sensor;

import java.util.Map;

public interface Sensor {
    /**
     * Collects information from a specific source in the environment
     * @param context Additional parameters that might be needed for sensing
     * @return Map of collected data
     */
    Map<String, Object> sense(Map<String, Object> context);
    
    /**
     * @return The type of information this sensor can collect
     */
    String getSensorType();
}