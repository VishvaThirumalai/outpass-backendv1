package com.mit.outpass.entity;

import com.mit.outpass.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Warden entity extending User
 */
@Entity
@Table(name = "wardens")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("WARDEN")
public class Warden extends User {
    
    @NotBlank(message = "Employee ID is required")
    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;
    
    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;
    
    @NotBlank(message = "Designation is required")
    @Column(nullable = false)
    private String designation;
    
    @Column(name = "hostel_assigned")
    private String hostelAssigned;
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "office_location")
    private String officeLocation;
    
    @Column(name = "office_hours")
    private String officeHours;
    
    // Default Constructor
    public Warden() {
        super();
        setRole(UserRole.WARDEN);
    }
    
    // Constructor
    public Warden(String username, String password, String fullName, String email,
                  String mobileNumber, String employeeId, String department,
                  String designation, String hostelAssigned) {
        super(username, password, fullName, email, mobileNumber, UserRole.WARDEN);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.hostelAssigned = hostelAssigned;
    }
    
    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public String getHostelAssigned() {
        return hostelAssigned;
    }
    
    public void setHostelAssigned(String hostelAssigned) {
        this.hostelAssigned = hostelAssigned;
    }
    
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
    
    public String getOfficeLocation() {
        return officeLocation;
    }
    
    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }
    
    public String getOfficeHours() {
        return officeHours;
    }
    
    public void setOfficeHours(String officeHours) {
        this.officeHours = officeHours;
    }
    
    @Override
    public String toString() {
        return "Warden{" +
                "employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", hostelAssigned='" + hostelAssigned + '\'' +
                "} " + super.toString();
    }
}