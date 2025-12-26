package com.smartincident.service;

import com.smartincident.model.Metric;
import com.smartincident.model.enums.IncidentSeverity;
import com.smartincident.model.enums.IncidentType;
import com.smartincident.repository.LogEntryRepository;
import com.smartincident.repository.MetricRepository;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Service for detecting anomalies in logs and metrics.
 * Uses configurable rule-based detection.
 */
@ApplicationScoped
public class AnomalyDetectionService {

    @Inject
    MetricRepository metricRepository;

    @Inject
    LogEntryRepository logEntryRepository;

    @Inject
    IncidentService incidentService;

    @ConfigProperty(name = "app.detection.error-rate-threshold", defaultValue = "5.0")
    double errorRateThreshold;

    @ConfigProperty(name = "app.detection.latency-threshold-ms", defaultValue = "2000")
    double latencyThreshold;

    @ConfigProperty(name = "app.detection.cpu-threshold-percent", defaultValue = "90.0")
    double cpuThreshold;

    @ConfigProperty(name = "app.detection.memory-threshold-percent", defaultValue = "85.0")
    double memoryThreshold;

    @ConfigProperty(name = "app.detection.pod-restart-threshold", defaultValue = "3")
    int podRestartThreshold;

    /**
     * Run anomaly detection for all services.
     */
    public void detectAnomalies() {
        Log.debug("Running anomaly detection...");
        
        List<String> services = metricRepository.getDistinctServiceNames();
        
        for (String service : services) {
            detectAnomaliesForService(service);
        }
        
        Log.debugf("Anomaly detection completed for %d services", services.size());
    }

    /**
     * Detect anomalies for a specific service.
     */
    public void detectAnomaliesForService(String serviceName) {
        checkHighErrorRate(serviceName);
        checkHighLatency(serviceName);
        checkCpuUsage(serviceName);
        checkMemoryUsage(serviceName);
        checkPodRestarts(serviceName);
    }

    /**
     * Check for high HTTP 5xx error rate.
     */
    private void checkHighErrorRate(String serviceName) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        
        // Get error count from metrics
        OptionalDouble errorCount = metricRepository.getAverageValue("http_5xx_count", serviceName, since);
        OptionalDouble totalCount = metricRepository.getAverageValue("http_total_count", serviceName, since);
        
        if (errorCount.isPresent() && totalCount.isPresent() && totalCount.getAsDouble() > 0) {
            double errorRate = (errorCount.getAsDouble() / totalCount.getAsDouble()) * 100;
            
            if (errorRate > errorRateThreshold) {
                Log.infof("High error rate detected for %s: %.2f%%", serviceName, errorRate);
                
                String relatedMetrics = String.format(
                    "{\"http_5xx_count\": %.0f, \"http_total_count\": %.0f, \"error_rate_percent\": %.2f}",
                    errorCount.getAsDouble(), totalCount.getAsDouble(), errorRate);
                
                incidentService.createIncident(
                    IncidentType.HIGH_ERROR_RATE,
                    errorRate > 20 ? IncidentSeverity.CRITICAL : IncidentSeverity.HIGH,
                    serviceName,
                    String.format("HTTP 5xx error rate is %.2f%% (threshold: %.2f%%)", errorRate, errorRateThreshold),
                    relatedMetrics
                );
            }
        }

        // Also check error logs
        long errorLogs = logEntryRepository.countErrors(serviceName, since);
        if (errorLogs > 50) {  // More than 50 error logs in 5 minutes
            Log.infof("High error log count for %s: %d errors in 5 min", serviceName, errorLogs);
            
            incidentService.createIncident(
                IncidentType.HIGH_ERROR_RATE,
                IncidentSeverity.MEDIUM,
                serviceName,
                String.format("High error log volume: %d errors in last 5 minutes", errorLogs),
                String.format("{\"error_log_count\": %d}", errorLogs)
            );
        }
    }

    /**
     * Check for high response latency.
     */
    private void checkHighLatency(String serviceName) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        
        OptionalDouble avgLatency = metricRepository.getAverageValue("latency_ms", serviceName, since);
        
        if (avgLatency.isPresent() && avgLatency.getAsDouble() > latencyThreshold) {
            Log.infof("High latency detected for %s: %.2f ms", serviceName, avgLatency.getAsDouble());
            
            String relatedMetrics = String.format(
                "{\"avg_latency_ms\": %.2f, \"threshold_ms\": %.2f}",
                avgLatency.getAsDouble(), latencyThreshold);
            
            IncidentSeverity severity = avgLatency.getAsDouble() > latencyThreshold * 2 
                ? IncidentSeverity.HIGH 
                : IncidentSeverity.MEDIUM;
            
            incidentService.createIncident(
                IncidentType.HIGH_LATENCY,
                severity,
                serviceName,
                String.format("Average latency is %.2f ms (threshold: %.2f ms)", 
                             avgLatency.getAsDouble(), latencyThreshold),
                relatedMetrics
            );
        }
    }

    /**
     * Check for high CPU usage.
     */
    private void checkCpuUsage(String serviceName) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(3);
        
        OptionalDouble avgCpu = metricRepository.getAverageValue("cpu_usage_percent", serviceName, since);
        
        if (avgCpu.isPresent() && avgCpu.getAsDouble() > cpuThreshold) {
            Log.infof("High CPU usage for %s: %.2f%%", serviceName, avgCpu.getAsDouble());
            
            String relatedMetrics = String.format(
                "{\"cpu_usage_percent\": %.2f, \"threshold_percent\": %.2f}",
                avgCpu.getAsDouble(), cpuThreshold);
            
            incidentService.createIncident(
                IncidentType.RESOURCE_EXHAUSTION,
                IncidentSeverity.HIGH,
                serviceName,
                String.format("CPU usage is %.2f%% (threshold: %.2f%%)", avgCpu.getAsDouble(), cpuThreshold),
                relatedMetrics
            );
        }
    }

    /**
     * Check for high memory usage.
     */
    private void checkMemoryUsage(String serviceName) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(3);
        
        OptionalDouble avgMemory = metricRepository.getAverageValue("memory_usage_percent", serviceName, since);
        
        if (avgMemory.isPresent() && avgMemory.getAsDouble() > memoryThreshold) {
            Log.infof("High memory usage for %s: %.2f%%", serviceName, avgMemory.getAsDouble());
            
            String relatedMetrics = String.format(
                "{\"memory_usage_percent\": %.2f, \"threshold_percent\": %.2f}",
                avgMemory.getAsDouble(), memoryThreshold);
            
            IncidentSeverity severity = avgMemory.getAsDouble() > 95 
                ? IncidentSeverity.CRITICAL 
                : IncidentSeverity.HIGH;
            
            incidentService.createIncident(
                IncidentType.RESOURCE_EXHAUSTION,
                severity,
                serviceName,
                String.format("Memory usage is %.2f%% (threshold: %.2f%%)", 
                             avgMemory.getAsDouble(), memoryThreshold),
                relatedMetrics
            );
        }
    }

    /**
     * Check for frequent pod restarts.
     */
    private void checkPodRestarts(String serviceName) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        
        List<Metric> restartMetrics = metricRepository.findMetricForService(
            "pod_restart_count", serviceName, since);
        
        if (!restartMetrics.isEmpty()) {
            double totalRestarts = restartMetrics.stream()
                .mapToDouble(m -> m.value)
                .sum();
            
            if (totalRestarts >= podRestartThreshold) {
                Log.infof("Frequent pod restarts for %s: %.0f restarts in 10 min", 
                         serviceName, totalRestarts);
                
                String relatedMetrics = String.format(
                    "{\"restart_count\": %.0f, \"threshold\": %d, \"period_minutes\": 10}",
                    totalRestarts, podRestartThreshold);
                
                incidentService.createIncident(
                    IncidentType.POD_RESTART,
                    IncidentSeverity.CRITICAL,
                    serviceName,
                    String.format("%.0f pod restarts in last 10 minutes (threshold: %d)", 
                                 totalRestarts, podRestartThreshold),
                    relatedMetrics
                );
            }
        }
    }
}
