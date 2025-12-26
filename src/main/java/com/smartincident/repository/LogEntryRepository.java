package com.smartincident.repository;

import com.smartincident.model.LogEntry;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LogEntry entity operations.
 */
@ApplicationScoped
public class LogEntryRepository implements PanacheRepository<LogEntry> {

    /**
     * Find log entries by service name.
     */
    public List<LogEntry> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);
    }

    /**
     * Find log entries by level.
     */
    public List<LogEntry> findByLevel(String level) {
        return list("level", level);
    }

    /**
     * Find error logs for a service within a time range.
     */
    public List<LogEntry> findErrorsForService(String serviceName, LocalDateTime since) {
        return list("serviceName = ?1 and level = 'ERROR' and timestamp >= ?2", serviceName, since);
    }

    /**
     * Find recent logs.
     */
    public List<LogEntry> findRecent(LocalDateTime since) {
        return list("timestamp >= ?1 ORDER BY timestamp DESC", since);
    }

    /**
     * Count error logs for a service in given time range.
     */
    public long countErrors(String serviceName, LocalDateTime since) {
        return count("serviceName = ?1 and level = 'ERROR' and timestamp >= ?2", serviceName, since);
    }

    /**
     * Find logs by pod name.
     */
    public List<LogEntry> findByPodName(String podName) {
        return list("podName", podName);
    }

    /**
     * Search logs containing a specific message pattern.
     */
    public List<LogEntry> searchByMessage(String pattern) {
        return list("lower(message) like ?1", "%" + pattern.toLowerCase() + "%");
    }
}
