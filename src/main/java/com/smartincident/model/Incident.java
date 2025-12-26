package com.smartincident.model;

import com.smartincident.model.enums.IncidentSeverity;
import com.smartincident.model.enums.IncidentStatus;
import com.smartincident.model.enums.IncidentType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a detected incident in the system.
 * Incidents are created automatically when anomalies are detected.
 */
@Entity
@Table(name = "incident")
public class Incident extends PanacheEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IncidentStatus status;

    @Column(nullable = false)
    public LocalDateTime detectedAt;

    @Column(length = 500)
    public String probableCause;

    @Column(length = 1000)
    public String description;

    @Column(nullable = false)
    public String serviceName;

    @Column(length = 2000)
    public String relatedMetrics;

    public LocalDateTime resolvedAt;

    public String resolvedBy;

    @Column(length = 2000)
    public String resolutionNotes;

    /**
     * Default constructor required by JPA.
     */
    public Incident() {
        this.status = IncidentStatus.OPEN;
        this.detectedAt = LocalDateTime.now();
    }

    /**
     * Creates an incident with the specified type, severity, and service.
     */
    public Incident(IncidentType type, IncidentSeverity severity, String serviceName) {
        this();
        this.type = type;
        this.severity = severity;
        this.serviceName = serviceName;
        this.description = type.getDescription();
    }

    /**
     * Marks the incident as resolved.
     */
    public void resolve(String resolvedBy, String notes) {
        this.status = IncidentStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = notes;
    }

    /**
     * Marks the incident as under investigation.
     */
    public void investigate() {
        this.status = IncidentStatus.INVESTIGATING;
    }
}
