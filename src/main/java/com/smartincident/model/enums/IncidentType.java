package com.smartincident.model.enums;

/**
 * Types of incidents that can be detected by the system.
 */
public enum IncidentType {
    HIGH_ERROR_RATE("High rate of HTTP 5xx errors"),
    HIGH_LATENCY("Response latency exceeds threshold"),
    RESOURCE_EXHAUSTION("CPU or Memory usage critically high"),
    POD_RESTART("Frequent pod restarts detected");

    private final String description;

    IncidentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
