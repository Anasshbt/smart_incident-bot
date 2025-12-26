package com.smartincident.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a log entry ingested from applications or Kubernetes pods.
 */
@Entity
@Table(name = "log_entry", indexes = {
    @Index(name = "idx_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_log_service", columnList = "serviceName"),
    @Index(name = "idx_log_level", columnList = "level")
})
public class LogEntry extends PanacheEntity {

    @Column(nullable = false)
    public LocalDateTime timestamp;

    @Column(nullable = false, length = 10)
    public String level;  // INFO, WARN, ERROR, DEBUG

    @Column(nullable = false, length = 5000)
    public String message;

    @Column(nullable = false)
    public String serviceName;

    public String podName;

    public String namespace;

    public String traceId;

    public String spanId;

    /**
     * Default constructor required by JPA.
     */
    public LogEntry() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a log entry with the essential fields.
     */
    public LogEntry(String level, String message, String serviceName) {
        this();
        this.level = level;
        this.message = message;
        this.serviceName = serviceName;
    }

    /**
     * Checks if this log entry represents an error.
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(level);
    }

    /**
     * Checks if this log entry represents a warning.
     */
    public boolean isWarning() {
        return "WARN".equalsIgnoreCase(level) || "WARNING".equalsIgnoreCase(level);
    }
}
