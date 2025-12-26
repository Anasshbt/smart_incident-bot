package com.smartincident.resource;

import com.smartincident.dto.IncidentDTO;
import com.smartincident.dto.IncidentStatusUpdateDTO;
import com.smartincident.service.IncidentService;
import com.smartincident.service.IncidentService.IncidentStats;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * REST API for incident management.
 */
@Path("/api/incidents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidentResource {

    @Inject
    IncidentService incidentService;

    /**
     * Get all incidents.
     */
    @GET
    public Response getAllIncidents() {
        List<IncidentDTO> incidents = incidentService.getAllIncidents();
        return Response.ok(incidents).build();
    }

    /**
     * Get incident by ID.
     */
    @GET
    @Path("/{id}")
    public Response getIncidentById(@PathParam("id") Long id) {
        Optional<IncidentDTO> incident = incidentService.getIncidentById(id);
        
        if (incident.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Incident not found\"}")
                    .build();
        }
        
        return Response.ok(incident.get()).build();
    }

    /**
     * Get open incidents.
     */
    @GET
    @Path("/open")
    public Response getOpenIncidents() {
        List<IncidentDTO> incidents = incidentService.getOpenIncidents();
        return Response.ok(incidents).build();
    }

    /**
     * Get incidents by service name.
     */
    @GET
    @Path("/service/{serviceName}")
    public Response getIncidentsByService(@PathParam("serviceName") String serviceName) {
        List<IncidentDTO> incidents = incidentService.getIncidentsByService(serviceName);
        return Response.ok(incidents).build();
    }

    /**
     * Update incident status.
     */
    @PATCH
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") Long id, IncidentStatusUpdateDTO updateDTO) {
        if (updateDTO.status == null || updateDTO.status.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Status is required\"}")
                    .build();
        }

        try {
            Optional<IncidentDTO> updated = incidentService.updateStatus(id, updateDTO);
            
            if (updated.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Incident not found\"}")
                        .build();
            }
            
            return Response.ok(updated.get()).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid status. Use: OPEN, INVESTIGATING, or RESOLVED\"}")
                    .build();
        }
    }

    /**
     * Get incident statistics.
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        IncidentStats stats = incidentService.getStats();
        return Response.ok(stats).build();
    }
}
