package com.smartincident.service;

import com.smartincident.dto.IncidentDTO;
import com.smartincident.dto.IncidentStatusUpdateDTO;
import com.smartincident.model.Incident;
import com.smartincident.model.enums.IncidentSeverity;
import com.smartincident.model.enums.IncidentStatus;
import com.smartincident.model.enums.IncidentType;
import com.smartincident.repository.IncidentRepository;
import io.quarkus.logging.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing incidents.
 */
@ApplicationScoped
public class IncidentService {

    @Inject
    IncidentRepository incidentRepository;

    @Inject
    RootCauseAnalysisService rootCauseAnalysisService;

    @Inject
    AlertingService alertingService;

    /**
     * Create a new incident.
     */
    @Transactional
    public Incident createIncident(IncidentType type, IncidentSeverity severity, 
                                    String serviceName, String description, String relatedMetrics) {
        
        // Check if there's already an open incident for this service and type
        if (incidentRepository.hasOpenIncidentForService(serviceName, type)) {
            Log.infof("Open incident already exists for %s with type %s", serviceName, type);
            return null;
        }

        Incident incident = new Incident(type, severity, serviceName);
        incident.description = description != null ? description : type.getDescription();
        incident.relatedMetrics = relatedMetrics;
        
        // Analyze and set probable cause
        incident.probableCause = rootCauseAnalysisService.analyzeCause(incident);
        
        incidentRepository.persist(incident);
        
        Log.infof("Created incident #%d: [%s] %s for %s - Probable cause: %s", 
                  incident.id, severity, type, serviceName, incident.probableCause);
        
        // Send alert
        alertingService.sendAlert(incident);
        
        return incident;
    }

    /**
     * Get all incidents.
     */
    public List<IncidentDTO> getAllIncidents() {
        return incidentRepository.findAllOrderedByDetectedAt()
                .stream()
                .map(IncidentDTO::fromEntity)
                .toList();
    }

    /**
     * Get incident by ID.
     */
    public Optional<IncidentDTO> getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id);
        return incident != null ? Optional.of(IncidentDTO.fromEntity(incident)) : Optional.empty();
    }

    /**
     * Get open incidents.
     */
    public List<IncidentDTO> getOpenIncidents() {
        return incidentRepository.findOpenIncidents()
                .stream()
                .map(IncidentDTO::fromEntity)
                .toList();
    }

    /**
     * Get incidents by service name.
     */
    public List<IncidentDTO> getIncidentsByService(String serviceName) {
        return incidentRepository.findByServiceName(serviceName)
                .stream()
                .map(IncidentDTO::fromEntity)
                .toList();
    }

    /**
     * Update incident status.
     */
    @Transactional
    public Optional<IncidentDTO> updateStatus(Long id, IncidentStatusUpdateDTO updateDTO) {
        Incident incident = incidentRepository.findById(id);
        
        if (incident == null) {
            return Optional.empty();
        }

        IncidentStatus newStatus = IncidentStatus.valueOf(updateDTO.status.toUpperCase());
        
        switch (newStatus) {
            case INVESTIGATING:
                incident.investigate();
                break;
            case RESOLVED:
                incident.resolve(updateDTO.resolvedBy, updateDTO.resolutionNotes);
                break;
            case OPEN:
                incident.status = IncidentStatus.OPEN;
                break;
        }

        Log.infof("Updated incident #%d status to %s", id, newStatus);
        
        return Optional.of(IncidentDTO.fromEntity(incident));
    }

    /**
     * Get incident statistics.
     */
    public IncidentStats getStats() {
        long openCount = incidentRepository.count("status", IncidentStatus.OPEN);
        long investigatingCount = incidentRepository.count("status", IncidentStatus.INVESTIGATING);
        long resolvedCount = incidentRepository.count("status", IncidentStatus.RESOLVED);
        long criticalOpen = incidentRepository.countOpenBySeverity(IncidentSeverity.CRITICAL);
        long highOpen = incidentRepository.countOpenBySeverity(IncidentSeverity.HIGH);
        
        return new IncidentStats(openCount, investigatingCount, resolvedCount, criticalOpen, highOpen);
    }

    /**
     * Statistics about incidents.
     */
    public record IncidentStats(
        long open,
        long investigating,
        long resolved,
        long criticalOpen,
        long highOpen
    ) {}
}
