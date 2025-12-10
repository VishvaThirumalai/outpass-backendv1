package com.mit.outpass.controller;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.ForgotPasswordRequest;
import com.mit.outpass.dto.LoginRequest;
import com.mit.outpass.dto.LoginResponse;
import com.mit.outpass.dto.ResetPasswordRequest;
import com.mit.outpass.dto.ResetPasswordByIdRequest; // ADD THIS IMPORT
import com.mit.outpass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.authenticateUser(loginRequest);
            ApiResponse<LoginResponse> response = ApiResponse.success("Login successful", loginResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<LoginResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
    
    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.registerUser(registerRequest);
            ApiResponse<String> response = ApiResponse.success("User registered successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            authService.logout(token);
            ApiResponse<String> response = ApiResponse.success("Logout successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Logout failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Validate token endpoint
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            authService.validateToken(token);
            ApiResponse<String> response = ApiResponse.success("Token is valid");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Invalid token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
    
    /**
     * Change password endpoint
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long userId = authService.getUserIdFromToken(token);
            authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            
            ApiResponse<String> response = ApiResponse.success("Password changed successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Forgot password endpoint using institutional ID
     */
    @PostMapping("/forgot-password-by-id")
    public ResponseEntity<ApiResponse<String>> forgotPasswordById(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            System.out.println("🔐 Forgot password request for ID: " + request.getLoginId() + ", Role: " + request.getRole());
            
            // Verify user identity using institutional ID and mobile number
            boolean identityVerified = authService.verifyUserIdentityById(
                request.getLoginId(), 
                request.getRole(), 
                request.getMobileNumber()
            );
            
            if (!identityVerified) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("ID and mobile number do not match or user not found"));
            }
            
            // In a real application, you would generate and send a reset token via email/SMS
            // For now, we'll just return success
            ApiResponse<String> response = ApiResponse.success("Identity verified. You can now reset your password.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Identity verification failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Reset password using institutional ID (with mobile verification)
     */
    @PostMapping("/reset-password-by-id")
    public ResponseEntity<ApiResponse<String>> resetPasswordById(@Valid @RequestBody ResetPasswordByIdRequest request) {
        try {
            System.out.println("🔐 Password reset request for ID: " + request.getLoginId() + ", Role: " + request.getRole());
            
            // Verify user identity using institutional ID and mobile number
            boolean identityVerified = authService.verifyUserIdentityById(
                request.getLoginId(), 
                request.getRole(), 
                request.getMobileNumber()
            );
            
            if (!identityVerified) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("ID and mobile number do not match or user not found"));
            }
            
            // Reset password using institutional ID
            authService.resetPasswordById(request.getLoginId(), request.getRole(), request.getNewPassword());
            
            ApiResponse<String> response = ApiResponse.success("Password reset successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Password reset failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Simple password reset (admin function - no mobile verification)
     */
    @PostMapping("/simple-reset-password")
    public ResponseEntity<ApiResponse<String>> simpleResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("🔐 Simple password reset for ID: " + request.getLoginId() + ", Role: " + request.getRole());
            
            // Reset password using institutional ID (no mobile verification for admin)
            authService.simpleResetPasswordById(request.getLoginId(), request.getRole(), request.getNewPassword());
            
            ApiResponse<String> response = ApiResponse.success("Password reset successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Password reset failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Inner class for change password request
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * Inner class for simple password reset request
     */
    public static class SimpleResetPasswordRequest {
        private String username;
        private String mobileNumber;
        private String newPassword;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    // In AuthController.java - update the RegisterRequest class with ALL fields
    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String mobileNumber;
        private String role;
        
        // Student specific fields
        private String rollNumber;
        private String course;
        private String degree;
        private Integer yearOfStudy;
        private String hostelName;
        private String roomNumber;
        private String address;
        private String guardianName;
        private String guardianMobile;
        private String guardianRelation;
        
        // Warden specific fields
        private String employeeId;
        private String department;
        private String designation;
        private String hostelAssigned;
        private Integer yearsOfExperience;
        private String officeLocation;
        private String officeHours;
        
        // Security specific fields
        private String securityId;
        private String shift;
        private String gateAssigned;
        private String supervisorName;
        private String supervisorContact;
        private Integer yearsOfService;
        private String securityClearanceLevel;
        
        // Admin specific fields
        private String adminId;
        private String permissionLevel;

        // Getters and Setters for ALL fields
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        // Student fields
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
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getGuardianName() { return guardianName; }
        public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
        
        public String getGuardianMobile() { return guardianMobile; }
        public void setGuardianMobile(String guardianMobile) { this.guardianMobile = guardianMobile; }
        
        public String getGuardianRelation() { return guardianRelation; }
        public void setGuardianRelation(String guardianRelation) { this.guardianRelation = guardianRelation; }
        
        // Warden fields
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        
        public String getHostelAssigned() { return hostelAssigned; }
        public void setHostelAssigned(String hostelAssigned) { this.hostelAssigned = hostelAssigned; }
        
        public Integer getYearsOfExperience() { return yearsOfExperience; }
        public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
        
        public String getOfficeLocation() { return officeLocation; }
        public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
        
        public String getOfficeHours() { return officeHours; }
        public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }
        
        // Security fields
        public String getSecurityId() { return securityId; }
        public void setSecurityId(String securityId) { this.securityId = securityId; }
        
        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }
        
        public String getGateAssigned() { return gateAssigned; }
        public void setGateAssigned(String gateAssigned) { this.gateAssigned = gateAssigned; }
        
        public String getSupervisorName() { return supervisorName; }
        public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }
        
        public String getSupervisorContact() { return supervisorContact; }
        public void setSupervisorContact(String supervisorContact) { this.supervisorContact = supervisorContact; }
        
        public Integer getYearsOfService() { return yearsOfService; }
        public void setYearsOfService(Integer yearsOfService) { this.yearsOfService = yearsOfService; }
        
        public String getSecurityClearanceLevel() { return securityClearanceLevel; }
        public void setSecurityClearanceLevel(String securityClearanceLevel) { this.securityClearanceLevel = securityClearanceLevel; }
        
        // Admin fields
        public String getAdminId() { return adminId; }
        public void setAdminId(String adminId) { this.adminId = adminId; }
        
        public String getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    }
}