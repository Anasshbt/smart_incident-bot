package com.smartincident.service;

import com.smartincident.model.Incident;
import com.smartincident.model.enums.IncidentType;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for analyzing incidents and determining probable root causes.
 * Uses rule-based correlation to suggest causes based on incident type and patterns.
 */
@ApplicationScoped
public class RootCauseAnalysisService {

    private static final Map<IncidentType, String[]> CAUSE_MAPPING = new HashMap<>();

    static {
        CAUSE_MAPPING.put(IncidentType.HIGH_ERROR_RATE, new String[]{
            "Database connection pool exhausted",
            "Downstream service unavailable",
            "Invalid request parameters from client",
            "Authentication/Authorization failures",
            "Network connectivity issues"
        });

        CAUSE_MAPPING.put(IncidentType.HIGH_LATENCY, new String[]{
            "Database slow queries",
            "External API timeout",
            "Network congestion",
            "Insufficient resources (CPU throttling)",
            "Lock contention in database"
        });

        CAUSE_MAPPING.put(IncidentType.RESOURCE_EXHAUSTION, new String[]{
            "Memory leak in application",
            "Resource limits set too low",
            "Traffic spike exceeding capacity",
            "Inefficient garbage collection",
            "Connection pool leak"
        });

        CAUSE_MAPPING.put(IncidentType.POD_RESTART, new String[]{
            "OOMKilled - Out of memory",
            "CrashLoopBackOff - Application crash",
            "Liveness probe failure",
            "Readiness probe failure",
            "Node pressure eviction"
        });
    }

    /**
     * Analyze an incident and determine the most probable cause.
     */
    public String analyzeCause(Incident incident) {
        String[] possibleCauses = CAUSE_MAPPING.get(incident.type);
        
        if (possibleCauses == null || possibleCauses.length == 0) {
            return "Unknown - requires manual investigation";
        }

        // Enhanced analysis based on related metrics and description
        return selectMostProbableCause(incident, possibleCauses);
    }

    /**
     * Get all possible causes for an incident type.
     */
    public String[] getPossibleCauses(IncidentType type) {
        return CAUSE_MAPPING.getOrDefault(type, new String[]{"Unknown"});
    }

    /**
     * Select the most probable cause based on incident context.
     */
    private String selectMostProbableCause(Incident incident, String[] causes) {
        String relatedMetrics = incident.relatedMetrics;
        String description = incident.description;

        // Simple keyword matching for cause selection
        if (relatedMetrics != null) {
            if (relatedMetrics.contains("memory") && containsAny(causes, "memory", "Memory", "OOM")) {
                return findCauseContaining(causes, "memory", "Memory", "OOM");
            }
            if (relatedMetrics.contains("cpu") && containsAny(causes, "CPU", "cpu", "throttl")) {
                return findCauseContaining(causes, "CPU", "cpu", "throttl");
            }
            if (relatedMetrics.contains("database") || relatedMetrics.contains("db")) {
                return findCauseContaining(causes, "Database", "database", "connection");
            }
        }

        if (description != null) {
            if (description.toLowerCase().contains("timeout")) {
                return findCauseContaining(causes, "timeout", "Timeout");
            }
            if (description.toLowerCase().contains("connection")) {
                return findCauseContaining(causes, "connection", "Connection");
            }
        }

        // Default to first cause
        return causes[0];
    }

    private boolean containsAny(String[] causes, String... keywords) {
        for (String cause : causes) {
            for (String keyword : keywords) {
                if (cause.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String findCauseContaining(String[] causes, String... keywords) {
        for (String cause : causes) {
            for (String keyword : keywords) {
                if (cause.contains(keyword)) {
                    return cause;
                }
            }
        }
        return causes[0];
    }
}
