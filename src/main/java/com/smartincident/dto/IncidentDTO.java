package com.smartincident.dto;

import com.smartincident.model.enums.IncidentSeverity;
import com.smartincident.model.enums.IncidentStatus;
import com.smartincident.model.enums.IncidentType;

import java.time.LocalDateTime;

/**
 * DTO for incident responses and updates.
 */
public class IncidentDTO {
    
    public Long id;
    public IncidentType type;
    public IncidentSeverity severity;
    public IncidentStatus status;
    public LocalDateTime detectedAt;
    public String probableCause;
    public String description;
    public String serviceName;
    public String relatedMetrics;
    public LocalDateTime resolvedAt;
    public String resolvedBy;
    public String resolutionNotes;

    public IncidentDTO() {
    }

    public static IncidentDTO fromEntity(com.smartincident.model.Incident incident) {
        IncidentDTO dto = new IncidentDTO();
        dto.id = incident.id;
        dto.type = incident.type;
        dto.severity = incident.severity;
        dto.status = incident.status;
        dto.detectedAt = incident.detectedAt;
        dto.probableCause = incident.probableCause;
        dto.description = incident.description;
        dto.serviceName = incident.serviceName;
        dto.relatedMetrics = incident.relatedMetrics;
        dto.resolvedAt = incident.resolvedAt;
        dto.resolvedBy = incident.resolvedBy;
        dto.resolutionNotes = incident.resolutionNotes;
        return dto;
    }
}
