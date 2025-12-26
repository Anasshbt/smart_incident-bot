package com.smartincident.resource;

import com.smartincident.dto.LogEntryDTO;
import com.smartincident.model.LogEntry;
import com.smartincident.service.LogIngestionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST API for log ingestion.
 */
@Path("/api/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogResource {

    @Inject
    LogIngestionService logIngestionService;

    /**
     * Ingest a single log entry.
     */
    @POST
    public Response ingestLog(LogEntryDTO logEntry) {
        if (logEntry.message == null || logEntry.message.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Message is required\"}")
                    .build();
        }
        if (logEntry.serviceName == null || logEntry.serviceName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Service name is required\"}")
                    .build();
        }

        LogEntry saved = logIngestionService.ingest(logEntry);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    /**
     * Ingest multiple log entries in batch.
     */
    @POST
    @Path("/batch")
    public Response ingestBatch(List<LogEntryDTO> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"At least one log entry is required\"}")
                    .build();
        }

        List<LogEntry> saved = logIngestionService.ingestBatch(logEntries);
        return Response.status(Response.Status.CREATED)
                .entity(new BatchResponse(saved.size(), "Log entries ingested successfully"))
                .build();
    }

    /**
     * Get recent logs.
     */
    @GET
    @Path("/recent")
    public Response getRecentLogs(@QueryParam("minutes") @DefaultValue("30") int minutes) {
        List<LogEntry> logs = logIngestionService.getRecentLogs(minutes);
        return Response.ok(logs).build();
    }

    /**
     * Get error logs for a service.
     */
    @GET
    @Path("/errors/{serviceName}")
    public Response getErrorLogs(
            @PathParam("serviceName") String serviceName,
            @QueryParam("minutes") @DefaultValue("60") int minutes) {
        
        List<LogEntry> errors = logIngestionService.getErrorsForService(serviceName, minutes);
        return Response.ok(errors).build();
    }

    /**
     * Response for batch operations.
     */
    public record BatchResponse(int count, String message) {}
}
