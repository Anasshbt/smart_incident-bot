package com.smartincident.dto;

import com.smartincident.model.enums.IncidentSeverity;
import com.smartincident.model.enums.IncidentType;

import java.time.LocalDateTime;

/**
 * Payload structure for webhook alerts.
 */
public class AlertPayload {
    
    public String alertId;
    public String title;
    public String message;
    public IncidentType incidentType;
    public IncidentSeverity severity;
    public String serviceName;
    public String probableCause;
    public LocalDateTime timestamp;
    public Long incidentId;
    public String dashboardUrl;

    public AlertPayload() {
        this.timestamp = LocalDateTime.now();
        this.alertId = java.util.UUID.randomUUID().toString();
    }

    public static AlertPayload fromIncident(com.smartincident.model.Incident incident) {
        AlertPayload payload = new AlertPayload();
        payload.incidentId = incident.id;
        payload.title = String.format("[%s] %s - %s", 
            incident.severity.name(), 
            incident.type.getDescription(), 
            incident.serviceName);
        payload.message = incident.description;
        payload.incidentType = incident.type;
        payload.severity = incident.severity;
        payload.serviceName = incident.serviceName;
        payload.probableCause = incident.probableCause;
        payload.timestamp = incident.detectedAt;
        return payload;
    }
}
