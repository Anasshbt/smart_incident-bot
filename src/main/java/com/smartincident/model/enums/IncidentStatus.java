package com.smartincident.model.enums;

/**
 * Status of an incident in its lifecycle.
 */
public enum IncidentStatus {
    OPEN("Incident detected and awaiting investigation"),
    INVESTIGATING("Incident is being investigated"),
    RESOLVED("Incident has been resolved");

    private final String description;

    IncidentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
