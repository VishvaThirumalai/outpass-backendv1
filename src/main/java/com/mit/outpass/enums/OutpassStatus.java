package com.mit.outpass.enums;

/**
 * Enum representing different statuses of an outpass application
 */
public enum OutpassStatus {
    PENDING("Pending", "Application is pending review"),
    APPROVED("Approved", "Application has been approved by warden"),
    REJECTED("Rejected", "Application has been rejected by warden"),
    ACTIVE("Active", "Student has left the hostel"),
    COMPLETED("Completed", "Student has returned to hostel"),
    EXPIRED("Expired", "Outpass has expired without return"),
    CANCELLED("Cancelled", "Application was cancelled by student");

    private final String displayName;
    private final String description;

    OutpassStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}