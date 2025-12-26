package com.smartincident.resource;

import com.smartincident.dto.MetricDTO;
import com.smartincident.model.Metric;
import com.smartincident.service.MetricsIngestionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST API for metrics ingestion.
 */
@Path("/api/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject
    MetricsIngestionService metricsIngestionService;

    /**
     * Ingest a single metric.
     */
    @POST
    public Response ingestMetric(MetricDTO metric) {
        if (metric.metricName == null || metric.metricName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Metric name is required\"}")
                    .build();
        }
        if (metric.value == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Value is required\"}")
                    .build();
        }
        if (metric.serviceName == null || metric.serviceName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Service name is required\"}")
                    .build();
        }

        Metric saved = metricsIngestionService.ingest(metric);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    /**
     * Ingest multiple metrics in batch.
     */
    @POST
    @Path("/batch")
    public Response ingestBatch(List<MetricDTO> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"At least one metric is required\"}")
                    .build();
        }

        List<Metric> saved = metricsIngestionService.ingestBatch(metrics);
        return Response.status(Response.Status.CREATED)
                .entity(new BatchResponse(saved.size(), "Metrics ingested successfully"))
                .build();
    }

    /**
     * Get metrics for a service.
     */
    @GET
    @Path("/service/{serviceName}")
    public Response getMetricsForService(
            @PathParam("serviceName") String serviceName,
            @QueryParam("minutes") @DefaultValue("60") int minutes) {
        
        List<Metric> metrics = metricsIngestionService.getMetricsForService(serviceName, minutes);
        return Response.ok(metrics).build();
    }

    /**
     * Get latest metric value.
     */
    @GET
    @Path("/latest/{metricName}/{serviceName}")
    public Response getLatestMetric(
            @PathParam("metricName") String metricName,
            @PathParam("serviceName") String serviceName) {
        
        Metric metric = metricsIngestionService.getLatestMetric(metricName, serviceName);
        
        if (metric == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"No metric found\"}")
                    .build();
        }
        
        return Response.ok(metric).build();
    }

    /**
     * Get all services reporting metrics.
     */
    @GET
    @Path("/services")
    public Response getAllServices() {
        List<String> services = metricsIngestionService.getAllServices();
        return Response.ok(services).build();
    }

    /**
     * Response for batch operations.
     */
    public record BatchResponse(int count, String message) {}
}
