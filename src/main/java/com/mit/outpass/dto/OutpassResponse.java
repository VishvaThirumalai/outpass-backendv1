package com.mit.outpass.dto;

import java.time.LocalDateTime;
import com.mit.outpass.enums.OutpassStatus;

public class OutpassResponse {
    private Long id;
    private String studentName;
    private String studentRollNumber;
    private String hostelName; // Add this field
    private String reason;
    private LocalDateTime leaveStartDate;
    private LocalDateTime expectedReturnDate;
    private String destination;
    private OutpassStatus status;
    private String wardenComments;
    private String securityComments;
    private LocalDateTime createdAt;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualReturnTime;
    private Boolean isLateReturn;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelation; // Add this field
    private String reviewedByName; // Add this field
    private String lateReturnReason;
    public OutpassResponse() {}
    
    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }
    
    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getLeaveStartDate() { return leaveStartDate; }
    public void setLeaveStartDate(LocalDateTime leaveStartDate) { this.leaveStartDate = leaveStartDate; }
    
    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public OutpassStatus getStatus() { return status; }
    public void setStatus(OutpassStatus status) { this.status = status; }
    
    public String getWardenComments() { return wardenComments; }
    public void setWardenComments(String wardenComments) { this.wardenComments = wardenComments; }
    
    public String getSecurityComments() { return securityComments; }
    public void setSecurityComments(String securityComments) { this.securityComments = securityComments; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getActualDepartureTime() { return actualDepartureTime; }
    public void setActualDepartureTime(LocalDateTime actualDepartureTime) { this.actualDepartureTime = actualDepartureTime; }
    
    public LocalDateTime getActualReturnTime() { return actualReturnTime; }
    public void setActualReturnTime(LocalDateTime actualReturnTime) { this.actualReturnTime = actualReturnTime; }
    
    public Boolean getIsLateReturn() { return isLateReturn; }
    public void setIsLateReturn(Boolean isLateReturn) { this.isLateReturn = isLateReturn; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }
    
    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }
    
    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public String getLateReturnReason() { return lateReturnReason; }
    public void setLateReturnReason(String lateReturnReason) { this.lateReturnReason = lateReturnReason; 
    }
}