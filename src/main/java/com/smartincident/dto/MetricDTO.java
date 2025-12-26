package com.smartincident.dto;

import java.time.LocalDateTime;

/**
 * DTO for metric ingestion via REST API.
 */
public class MetricDTO {
    
    public LocalDateTime timestamp;
    public String metricName;
    public Double value;
    public String serviceName;
    public String podName;
    public String namespace;
    public String unit;

    public MetricDTO() {
    }

    public MetricDTO(String metricName, Double value, String serviceName) {
        this.metricName = metricName;
        this.value = value;
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
    }
}
