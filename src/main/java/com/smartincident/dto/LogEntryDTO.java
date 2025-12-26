package com.smartincident.dto;

import java.time.LocalDateTime;

/**
 * DTO for log entry ingestion via REST API.
 */
public class LogEntryDTO {
    
    public LocalDateTime timestamp;
    public String level;
    public String message;
    public String serviceName;
    public String podName;
    public String namespace;
    public String traceId;
    public String spanId;

    public LogEntryDTO() {
    }

    public LogEntryDTO(String level, String message, String serviceName) {
        this.level = level;
        this.message = message;
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
    }
}
