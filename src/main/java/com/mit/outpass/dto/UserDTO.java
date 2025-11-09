package com.mit.outpass.dto;

import com.mit.outpass.enums.UserRole;
import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private UserRole role;
    private boolean isActive;
    private String createdAt;
    private LocalDateTime lastLogin;
    
    // Student specific
    private String rollNumber;
    private String course;
    private String degree;
    private Integer yearOfStudy;
    private String hostelName;
    private String roomNumber;
    
    // Warden specific
    private String employeeId;
    private String department;
    private String designation;
    private String hostelAssigned;
    
    // Security specific
    private String securityId;
    private String shift;
    private String gateAssigned;
    
    // Admin specific
    private String adminId;
    private String permissionLevel;
    
    // Constructors
    public UserDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    
    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getHostelAssigned() { return hostelAssigned; }
    public void setHostelAssigned(String hostelAssigned) { this.hostelAssigned = hostelAssigned; }
    
    public String getSecurityId() { return securityId; }
    public void setSecurityId(String securityId) { this.securityId = securityId; }
    
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    
    public String getGateAssigned() { return gateAssigned; }
    public void setGateAssigned(String gateAssigned) { this.gateAssigned = gateAssigned; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
}