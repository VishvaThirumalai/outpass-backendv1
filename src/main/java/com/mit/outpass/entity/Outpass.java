package com.mit.outpass.entity;

import com.mit.outpass.enums.OutpassStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outpasses")
public class Outpass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student who applied for the outpass
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDateTime leaveStartDate;

    @Column(nullable = false)
    private LocalDateTime expectedReturnDate;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutpassStatus status = OutpassStatus.PENDING;

    // Emergency contact details
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelation;

    // Warden review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Warden reviewedBy;

    @Column(length = 500)
    private String wardenComments;

    @Column(length = 500)
    private String securityComments;

    // Security actions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_marked_by")
    private Security departureMarkedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_marked_by")
    private Security returnMarkedBy;

    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualReturnTime;

    private Boolean isLateReturn;

    // ADD THIS FIELD FOR LATE RETURN REASON
    @Column(name = "late_return_reason", length = 500)
    private String lateReturnReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String destination;
    
    private LocalDateTime reviewedAt;

    // Default Constructor
    public Outpass() {}

    // Constructor with essential fields
    public Outpass(Student student, LocalDateTime leaveStartDate, LocalDateTime expectedReturnDate, 
                   String reason, String destination) {
        this.student = student;
        this.leaveStartDate = leaveStartDate;
        this.expectedReturnDate = expectedReturnDate;
        this.reason = reason;
        this.destination = destination;
        this.status = OutpassStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDateTime getLeaveStartDate() { return leaveStartDate; }
    public void setLeaveStartDate(LocalDateTime leaveStartDate) { this.leaveStartDate = leaveStartDate; }

    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OutpassStatus getStatus() { return status; }
    public void setStatus(OutpassStatus status) { this.status = status; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }

    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }

    public Warden getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Warden reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getWardenComments() { return wardenComments; }
    public void setWardenComments(String wardenComments) { this.wardenComments = wardenComments; }

    public String getSecurityComments() { return securityComments; }
    public void setSecurityComments(String securityComments) { this.securityComments = securityComments; }

    public Security getDepartureMarkedBy() { return departureMarkedBy; }
    public void setDepartureMarkedBy(Security departureMarkedBy) { this.departureMarkedBy = departureMarkedBy; }

    public Security getReturnMarkedBy() { return returnMarkedBy; }
    public void setReturnMarkedBy(Security returnMarkedBy) { this.returnMarkedBy = returnMarkedBy; }

    public LocalDateTime getActualDepartureTime() { return actualDepartureTime; }
    public void setActualDepartureTime(LocalDateTime actualDepartureTime) { this.actualDepartureTime = actualDepartureTime; }

    public LocalDateTime getActualReturnTime() { return actualReturnTime; }
    public void setActualReturnTime(LocalDateTime actualReturnTime) { this.actualReturnTime = actualReturnTime; }

    public Boolean getIsLateReturn() { return isLateReturn; }
    public void setIsLateReturn(Boolean isLateReturn) { this.isLateReturn = isLateReturn; }

    // ADD GETTER AND SETTER FOR LATE RETURN REASON
    public String getLateReturnReason() { return lateReturnReason; }
    public void setLateReturnReason(String lateReturnReason) { this.lateReturnReason = lateReturnReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    /**
     * Utility methods to check if editing or cancelling is allowed
     */
    public boolean canBeEdited() {
        return this.status == OutpassStatus.PENDING;
    }

    public boolean canBeCancelled() {
        return this.status == OutpassStatus.PENDING || this.status == OutpassStatus.APPROVED;
    }

    @Override
    public String toString() {
        return "Outpass{" +
                "id=" + id +
                ", student=" + (student != null ? student.getRollNumber() : "null") +
                ", status=" + status +
                ", destination='" + destination + '\'' +
                ", isLateReturn=" + isLateReturn +
                ", lateReturnReason='" + lateReturnReason + '\'' +
                '}';
    }
}