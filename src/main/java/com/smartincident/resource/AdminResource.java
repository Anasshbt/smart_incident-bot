package com.smartincident.resource;

import com.smartincident.service.AlertingService;
import com.smartincident.service.AnomalyDetectionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for system management and testing.
 */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    AlertingService alertingService;

    @Inject
    AnomalyDetectionService anomalyDetectionService;

    /**
     * Test the alerting webhook.
     */
    @POST
    @Path("/test-alert")
    public Response testAlert() {
        boolean success = alertingService.sendTestAlert();
        
        if (success) {
            return Response.ok("{\"message\": \"Test alert sent successfully\"}").build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\": \"Failed to send test alert\"}")
                    .build();
        }
    }

    /**
     * Manually trigger anomaly detection.
     */
    @POST
    @Path("/trigger-detection")
    public Response triggerDetection() {
        try {
            anomalyDetectionService.detectAnomalies();
            return Response.ok("{\"message\": \"Anomaly detection triggered\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Detection failed: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get application info.
     */
    @GET
    @Path("/info")
    public Response getInfo() {
        return Response.ok(new AppInfo(
            "Smart Incident Bot",
            "1.0.0",
            "Cloud-native incident detection and diagnosis system",
            System.getProperty("java.version"),
            Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB"
        )).build();
    }

    /**
     * Application information.
     */
    public record AppInfo(
        String name,
        String version,
        String description,
        String javaVersion,
        String maxMemory
    ) {}
}
