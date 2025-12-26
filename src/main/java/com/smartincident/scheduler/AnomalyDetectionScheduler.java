package com.smartincident.scheduler;

import com.smartincident.service.AnomalyDetectionService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Scheduler for periodic anomaly detection.
 */
@ApplicationScoped
public class AnomalyDetectionScheduler {

    @Inject
    AnomalyDetectionService anomalyDetectionService;

    @ConfigProperty(name = "app.detection.check-interval-seconds", defaultValue = "30")
    int checkIntervalSeconds;

    /**
     * Run anomaly detection periodically.
     * Default: every 30 seconds
     */
    @Scheduled(every = "${app.detection.check-interval-seconds:30}s")
    void runDetection() {
        Log.debug("Scheduled anomaly detection triggered");
        try {
            anomalyDetectionService.detectAnomalies();
        } catch (Exception e) {
            Log.errorf("Error during scheduled anomaly detection: %s", e.getMessage());
        }
    }
}
