package com.smartincident.repository;

import com.smartincident.model.Incident;
import com.smartincident.model.enums.IncidentStatus;
import com.smartincident.model.enums.IncidentType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Incident entity operations.
 */
@ApplicationScoped
public class IncidentRepository implements PanacheRepository<Incident> {

    /**
     * Find all open incidents.
     */
    public List<Incident> findOpenIncidents() {
        return list("status", IncidentStatus.OPEN);
    }

    /**
     * Find incidents by status.
     */
    public List<Incident> findByStatus(IncidentStatus status) {
        return list("status", status);
    }

    /**
     * Find incidents by service name.
     */
    public List<Incident> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);
    }

    /**
     * Find incidents by type.
     */
    public List<Incident> findByType(IncidentType type) {
        return list("type", type);
    }

    /**
     * Find incidents detected after a certain time.
     */
    public List<Incident> findRecentIncidents(LocalDateTime since) {
        return list("detectedAt >= ?1", since);
    }

    /**
     * Check if an open incident of the same type exists for a service.
     */
    public boolean hasOpenIncidentForService(String serviceName, IncidentType type) {
        return count("serviceName = ?1 and type = ?2 and status = ?3", 
                     serviceName, type, IncidentStatus.OPEN) > 0;
    }

    /**
     * Find incidents ordered by detection time (newest first).
     */
    public List<Incident> findAllOrderedByDetectedAt() {
        return list("ORDER BY detectedAt DESC");
    }

    /**
     * Count open incidents by severity.
     */
    public long countOpenBySeverity(com.smartincident.model.enums.IncidentSeverity severity) {
        return count("status = ?1 and severity = ?2", IncidentStatus.OPEN, severity);
    }
}
