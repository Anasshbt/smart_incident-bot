package com.smartincident.service;

import com.smartincident.dto.AlertPayload;
import com.smartincident.model.Incident;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Service for sending alerts via webhooks.
 */
@ApplicationScoped
public class AlertingService {

    @ConfigProperty(name = "app.alerting.webhook-url")
    String webhookUrl;

    @ConfigProperty(name = "app.alerting.enabled", defaultValue = "true")
    boolean alertingEnabled;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Inject
    public AlertingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Send an alert for an incident.
     */
    public void sendAlert(Incident incident) {
        if (!alertingEnabled) {
            Log.debug("Alerting is disabled, skipping alert");
            return;
        }

        try {
            AlertPayload payload = AlertPayload.fromIncident(incident);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            // Send asynchronously to not block incident creation
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            Log.infof("Alert sent successfully for incident #%d", incident.id);
                        } else {
                            Log.warnf("Alert webhook returned status %d for incident #%d", 
                                      response.statusCode(), incident.id);
                        }
                    })
                    .exceptionally(ex -> {
                        Log.warnf("Failed to send alert for incident #%d: %s", 
                                  incident.id, ex.getMessage());
                        return null;
                    });

            Log.debugf("Alert request sent for incident #%d to %s", incident.id, webhookUrl);
            
        } catch (Exception e) {
            Log.errorf("Error sending alert for incident #%d: %s", incident.id, e.getMessage());
        }
    }

    /**
     * Send a test alert to verify webhook configuration.
     */
    public boolean sendTestAlert() {
        try {
            String testPayload = """
                {
                    "alertId": "test-alert",
                    "title": "Test Alert",
                    "message": "This is a test alert from Smart Incident Bot",
                    "severity": "LOW",
                    "timestamp": "%s"
                }
                """.formatted(java.time.LocalDateTime.now().toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(testPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            Log.infof("Test alert sent, status: %d, success: %b", response.statusCode(), success);
            
            return success;
            
        } catch (Exception e) {
            Log.errorf("Error sending test alert: %s", e.getMessage());
            return false;
        }
    }
}
