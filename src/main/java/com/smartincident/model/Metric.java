package com.smartincident.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a metric data point collected from services or infrastructure.
 */
@Entity
@Table(name = "metric", indexes = {
    @Index(name = "idx_metric_timestamp", columnList = "timestamp"),
    @Index(name = "idx_metric_name", columnList = "metricName"),
    @Index(name = "idx_metric_service", columnList = "serviceName")
})
public class Metric extends PanacheEntity {

    @Column(nullable = false)
    public LocalDateTime timestamp;

    @Column(nullable = false)
    public String metricName;  // cpu_usage_percent, memory_usage_percent, http_5xx_count, latency_ms

    @Column(nullable = false)
    public Double value;

    @Column(nullable = false)
    public String serviceName;

    public String podName;

    public String namespace;

    public String unit;  // percent, milliseconds, count, bytes

    /**
     * Default constructor required by JPA.
     */
    public Metric() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a metric with the essential fields.
     */
    public Metric(String metricName, Double value, String serviceName) {
        this();
        this.metricName = metricName;
        this.value = value;
        this.serviceName = serviceName;
    }

    /**
     * Creates a metric with all common fields.
     */
    public Metric(String metricName, Double value, String serviceName, String podName, String unit) {
        this(metricName, value, serviceName);
        this.podName = podName;
        this.unit = unit;
    }

    /**
     * Checks if this metric exceeds the given threshold.
     */
    public boolean exceedsThreshold(double threshold) {
        return value != null && value > threshold;
    }
}
