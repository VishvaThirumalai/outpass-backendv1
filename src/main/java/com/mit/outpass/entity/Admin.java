package com.mit.outpass.entity;

import com.mit.outpass.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    
    @Column(name = "admin_id", unique = true, nullable = false)
    private String adminId;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "designation")
    private String designation;
    
    @Column(name = "permission_level")
    private String permissionLevel;
    
    // Default Constructor
    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }
    
    // Constructor
    public Admin(String username, String password, String fullName, String email,
                 String mobileNumber, String adminId) {
        super(username, password, fullName, email, mobileNumber, UserRole.ADMIN);
        this.adminId = adminId;
    }
    
    // Getters and Setters
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", department='" + department + '\'' +
                "} " + super.toString();
    }
}