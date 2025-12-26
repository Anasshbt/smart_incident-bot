package com.smartincident.dto;

/**
 * DTO for updating incident status.
 */
public class IncidentStatusUpdateDTO {
    
    public String status;  // OPEN, INVESTIGATING, RESOLVED
    public String resolvedBy;
    public String resolutionNotes;

    public IncidentStatusUpdateDTO() {
    }
}
