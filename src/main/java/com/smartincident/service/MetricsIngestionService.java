package com.smartincident.service;

import com.smartincident.dto.MetricDTO;
import com.smartincident.model.Metric;
import com.smartincident.repository.MetricRepository;
import io.quarkus.logging.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Service for ingesting and processing metrics.
 */
@ApplicationScoped
public class MetricsIngestionService {

    @Inject
    MetricRepository metricRepository;

    /**
     * Ingest a single metric.
     */
    @Transactional
    public Metric ingest(MetricDTO dto) {
        Metric metric = toEntity(dto);
        metricRepository.persist(metric);
        
        Log.debugf("Ingested metric: %s = %.2f for %s", 
                   metric.metricName, 
                   metric.value, 
                   metric.serviceName);
        
        return metric;
    }

    /**
     * Ingest multiple metrics in batch.
     */
    @Transactional
    public List<Metric> ingestBatch(List<MetricDTO> dtos) {
        List<Metric> metrics = dtos.stream()
                .map(this::toEntity)
                .toList();
        
        for (Metric metric : metrics) {
            metricRepository.persist(metric);
        }
        
        Log.infof("Ingested batch of %d metrics", metrics.size());
        return metrics;
    }

    /**
     * Get average value of a metric for a service in the last N minutes.
     */
    public OptionalDouble getAverageMetric(String metricName, String serviceName, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return metricRepository.getAverageValue(metricName, serviceName, since);
    }

    /**
     * Get latest metric value for a service.
     */
    public Metric getLatestMetric(String metricName, String serviceName) {
        return metricRepository.getLatestMetric(metricName, serviceName);
    }

    /**
     * Get metrics exceeding a threshold in the last N minutes.
     */
    public List<Metric> getMetricsExceedingThreshold(String metricName, double threshold, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return metricRepository.findExceedingThreshold(metricName, threshold, since);
    }

    /**
     * Get all services that have reported metrics.
     */
    public List<String> getAllServices() {
        return metricRepository.getDistinctServiceNames();
    }

    /**
     * Get metrics for a service in the last N minutes.
     */
    public List<Metric> getMetricsForService(String serviceName, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return metricRepository.findByServiceAndTimeRange(serviceName, since);
    }

    private Metric toEntity(MetricDTO dto) {
        Metric metric = new Metric();
        metric.timestamp = dto.timestamp != null ? dto.timestamp : LocalDateTime.now();
        metric.metricName = dto.metricName;
        metric.value = dto.value;
        metric.serviceName = dto.serviceName;
        metric.podName = dto.podName;
        metric.namespace = dto.namespace;
        metric.unit = dto.unit;
        return metric;
    }
}
