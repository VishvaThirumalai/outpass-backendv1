package com.mit.outpass.enums;

/**
 * Enum representing different user roles in the outpass management system
 */
public enum UserRole {
    STUDENT("Student"),
    WARDEN("Warden"), 
    SECURITY("Security"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}