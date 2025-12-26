package com.smartincident.service;

import com.smartincident.dto.LogEntryDTO;
import com.smartincident.model.LogEntry;
import com.smartincident.repository.LogEntryRepository;
import io.quarkus.logging.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for ingesting and processing log entries.
 */
@ApplicationScoped
public class LogIngestionService {

    @Inject
    LogEntryRepository logEntryRepository;

    /**
     * Ingest a single log entry.
     */
    @Transactional
    public LogEntry ingest(LogEntryDTO dto) {
        LogEntry logEntry = new LogEntry();
        logEntry.timestamp = dto.timestamp != null ? dto.timestamp : LocalDateTime.now();
        logEntry.level = dto.level != null ? dto.level.toUpperCase() : "INFO";
        logEntry.message = dto.message;
        logEntry.serviceName = dto.serviceName;
        logEntry.podName = dto.podName;
        logEntry.namespace = dto.namespace;
        logEntry.traceId = dto.traceId;
        logEntry.spanId = dto.spanId;

        logEntryRepository.persist(logEntry);
        
        Log.debugf("Ingested log entry: [%s] %s from %s", 
                   logEntry.level, 
                   truncate(logEntry.message, 50), 
                   logEntry.serviceName);
        
        return logEntry;
    }

    /**
     * Ingest multiple log entries in batch.
     */
    @Transactional
    public List<LogEntry> ingestBatch(List<LogEntryDTO> dtos) {
        List<LogEntry> entries = dtos.stream()
                .map(this::toEntity)
                .toList();
        
        for (LogEntry entry : entries) {
            logEntryRepository.persist(entry);
        }
        
        Log.infof("Ingested batch of %d log entries", entries.size());
        return entries;
    }

    /**
     * Get error logs for a service since a given time.
     */
    public List<LogEntry> getErrorsForService(String serviceName, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return logEntryRepository.findErrorsForService(serviceName, since);
    }

    /**
     * Count errors for a service in the last N minutes.
     */
    public long countRecentErrors(String serviceName, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return logEntryRepository.countErrors(serviceName, since);
    }

    /**
     * Get recent logs.
     */
    public List<LogEntry> getRecentLogs(int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return logEntryRepository.findRecent(since);
    }

    private LogEntry toEntity(LogEntryDTO dto) {
        LogEntry logEntry = new LogEntry();
        logEntry.timestamp = dto.timestamp != null ? dto.timestamp : LocalDateTime.now();
        logEntry.level = dto.level != null ? dto.level.toUpperCase() : "INFO";
        logEntry.message = dto.message;
        logEntry.serviceName = dto.serviceName;
        logEntry.podName = dto.podName;
        logEntry.namespace = dto.namespace;
        logEntry.traceId = dto.traceId;
        logEntry.spanId = dto.spanId;
        return logEntry;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
