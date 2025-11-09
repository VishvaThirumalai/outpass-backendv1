package com.mit.outpass.entity;

import com.mit.outpass.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Student entity extending User
 */
@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("STUDENT")
public class Student extends User {
    
    @NotBlank(message = "Roll number is required")
    @Pattern(regexp = "^MIT[0-9]{4}[0-9]{3}$", message = "Roll number format: MITYYYY###")
    @Column(name = "roll_number", unique = true, nullable = false)
    private String rollNumber;
    
    @NotBlank(message = "Course is required")
    @Column(nullable = false)
    private String course;
    
    @NotBlank(message = "Degree is required")
    @Column(nullable = false)
    private String degree;
    
    @Column(name = "year_of_study")
    private Integer yearOfStudy;
    
    @NotBlank(message = "Hostel name is required")
    @Column(name = "hostel_name", nullable = false)
    private String hostelName;
    
    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", nullable = false)
    private String roomNumber;
    
    @Column(columnDefinition = "TEXT")
    private String address; // Changed to optional
    
    @Column(name = "guardian_name")
    private String guardianName;
    
    @Column(name = "guardian_mobile")
    @Pattern(regexp = "^[0-9]{10}$", message = "Guardian mobile must be 10 digits")
    private String guardianMobile;
    
    @Column(name = "guardian_relation")
    private String guardianRelation;
    
    // One-to-Many relationship with Outpass
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Outpass> outpasses;
    
    // Default Constructor
    public Student() {
        super();
        setRole(UserRole.STUDENT);
    }
    
    // Constructor
    public Student(String username, String password, String fullName, String email,
                   String mobileNumber, String rollNumber, String course, String degree,
                   String hostelName, String roomNumber, String address) {
        super(username, password, fullName, email, mobileNumber, UserRole.STUDENT);
        this.rollNumber = rollNumber;
        this.course = course;
        this.degree = degree;
        this.hostelName = hostelName;
        this.roomNumber = roomNumber;
        this.address = address;
    }
    
    // Getters and Setters
    public String getRollNumber() {
        return rollNumber;
    }
    
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
    
    public String getCourse() {
        return course;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
    
    public String getDegree() {
        return degree;
    }
    
    public void setDegree(String degree) {
        this.degree = degree;
    }
    
    public Integer getYearOfStudy() {
        return yearOfStudy;
    }
    
    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
    
    public String getHostelName() {
        return hostelName;
    }
    
    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getGuardianName() {
        return guardianName;
    }
    
    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }
    
    public String getGuardianMobile() {
        return guardianMobile;
    }
    
    public void setGuardianMobile(String guardianMobile) {
        this.guardianMobile = guardianMobile;
    }
    
    public String getGuardianRelation() {
        return guardianRelation;
    }
    
    public void setGuardianRelation(String guardianRelation) {
        this.guardianRelation = guardianRelation;
    }
    
    public List<Outpass> getOutpasses() {
        return outpasses;
    }
    
    public void setOutpasses(List<Outpass> outpasses) {
        this.outpasses = outpasses;
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "rollNumber='" + rollNumber + '\'' +
                ", course='" + course + '\'' +
                ", degree='" + degree + '\'' +
                ", hostelName='" + hostelName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                "} " + super.toString();
    }
}