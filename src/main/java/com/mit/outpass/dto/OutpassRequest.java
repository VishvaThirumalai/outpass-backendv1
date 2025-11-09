package com.mit.outpass.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class OutpassRequest {
    
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
    
    @NotNull(message = "Leave start date is required")
    // REMOVE THIS LINE: @Future(message = "Leave start date must be in future")
    private LocalDateTime leaveStartDate;
    
    @NotNull(message = "Expected return date is required")
    private LocalDateTime expectedReturnDate;
    
    @Size(max = 200, message = "Destination cannot exceed 200 characters")
    private String destination;
    
    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    private String emergencyContactName;
    
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact number must be 10 digits or empty")
    private String emergencyContactNumber;
    
    @Size(max = 50, message = "Emergency contact relation cannot exceed 50 characters")
    private String emergencyContactRelation;
    
    public OutpassRequest() {}
    
    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getLeaveStartDate() { return leaveStartDate; }
    public void setLeaveStartDate(LocalDateTime leaveStartDate) { this.leaveStartDate = leaveStartDate; }
    
    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }
    
    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }
}