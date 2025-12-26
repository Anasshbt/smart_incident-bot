package com.smartincident.model.enums;

/**
 * Severity levels for incidents.
 */
public enum IncidentSeverity {
    LOW(1, "Minor issue, can be addressed during normal operations"),
    MEDIUM(2, "Moderate issue requiring attention"),
    HIGH(3, "Significant issue affecting service quality"),
    CRITICAL(4, "Critical issue requiring immediate action");

    private final int level;
    private final String description;

    IncidentSeverity(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}
