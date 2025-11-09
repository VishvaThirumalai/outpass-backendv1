package com.mit.outpass.entity;

import com.mit.outpass.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Security entity extending User
 */
@Entity
@Table(name = "security_personnel")
@PrimaryKeyJoinColumn(name = "user_id")
public class Security extends User {
    
    @NotBlank(message = "Security ID is required")
    @Column(name = "security_id", unique = true, nullable = false)
    private String securityId;
    
    @NotBlank(message = "Shift is required")
    @Column(nullable = false)
    private String shift;
    
    @Column(name = "gate_assigned")
    private String gateAssigned;
    
    @Column(name = "supervisor_name")
    private String supervisorName;
    
    @Column(name = "supervisor_contact")
    private String supervisorContact;
    
    @Column(name = "years_of_service")
    private Integer yearsOfService;
    
    @Column(name = "security_clearance_level")
    private String securityClearanceLevel;
    
    // Default Constructor
    public Security() {
        super();
        setRole(UserRole.SECURITY);
    }
    
    // Constructor
    public Security(String username, String password, String fullName, String email,
                    String mobileNumber, String securityId, String shift, String gateAssigned) {
        super(username, password, fullName, email, mobileNumber, UserRole.SECURITY);
        this.securityId = securityId;
        this.shift = shift;
        this.gateAssigned = gateAssigned;
    }
    
    // Getters and Setters
    public String getSecurityId() {
        return securityId;
    }
    
    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }
    
    public String getShift() {
        return shift;
    }
    
    public void setShift(String shift) {
        this.shift = shift;
    }
    
    public String getGateAssigned() {
        return gateAssigned;
    }
    
    public void setGateAssigned(String gateAssigned) {
        this.gateAssigned = gateAssigned;
    }
    
    public String getSupervisorName() {
        return supervisorName;
    }
    
    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }
    
    public String getSupervisorContact() {
        return supervisorContact;
    }
    
    public void setSupervisorContact(String supervisorContact) {
        this.supervisorContact = supervisorContact;
    }
    
    public Integer getYearsOfService() {
        return yearsOfService;
    }
    
    public void setYearsOfService(Integer yearsOfService) {
        this.yearsOfService = yearsOfService;
    }
    
    public String getSecurityClearanceLevel() {
        return securityClearanceLevel;
    }
    
    public void setSecurityClearanceLevel(String securityClearanceLevel) {
        this.securityClearanceLevel = securityClearanceLevel;
    }
    
    @Override
    public String toString() {
        return "Security{" +
                "securityId='" + securityId + '\'' +
                ", shift='" + shift + '\'' +
                ", gateAssigned='" + gateAssigned + '\'' +
                "} " + super.toString();
    }
}