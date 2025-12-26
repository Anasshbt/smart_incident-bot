package com.smartincident.repository;

import com.smartincident.model.Metric;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Repository for Metric entity operations.
 */
@ApplicationScoped
public class MetricRepository implements PanacheRepository<Metric> {

    /**
     * Find metrics by service name.
     */
    public List<Metric> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);
    }

    /**
     * Find metrics by metric name.
     */
    public List<Metric> findByMetricName(String metricName) {
        return list("metricName", metricName);
    }

    /**
     * Find metrics for a service within a time range.
     */
    public List<Metric> findByServiceAndTimeRange(String serviceName, LocalDateTime since) {
        return list("serviceName = ?1 and timestamp >= ?2", serviceName, since);
    }

    /**
     * Find specific metric for a service within a time range.
     */
    public List<Metric> findMetricForService(String metricName, String serviceName, LocalDateTime since) {
        return list("metricName = ?1 and serviceName = ?2 and timestamp >= ?3 ORDER BY timestamp DESC", 
                    metricName, serviceName, since);
    }

    /**
     * Get average value of a metric for a service in time range.
     */
    public OptionalDouble getAverageValue(String metricName, String serviceName, LocalDateTime since) {
        List<Metric> metrics = findMetricForService(metricName, serviceName, since);
        return metrics.stream()
                .mapToDouble(m -> m.value)
                .average();
    }

    /**
     * Get latest metric value for a service.
     */
    public Metric getLatestMetric(String metricName, String serviceName) {
        return find("metricName = ?1 and serviceName = ?2 ORDER BY timestamp DESC", 
                    metricName, serviceName)
                .firstResult();
    }

    /**
     * Find metrics exceeding a threshold.
     */
    public List<Metric> findExceedingThreshold(String metricName, double threshold, LocalDateTime since) {
        return list("metricName = ?1 and value > ?2 and timestamp >= ?3", 
                    metricName, threshold, since);
    }

    /**
     * Get distinct service names that have reported metrics.
     */
    public List<String> getDistinctServiceNames() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT m.serviceName FROM Metric m", String.class)
                .getResultList();
    }
}
