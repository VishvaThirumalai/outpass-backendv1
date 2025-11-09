package com.mit.outpass.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.OutpassRequest;
import com.mit.outpass.dto.OutpassResponse;
import com.mit.outpass.entity.Student;
import com.mit.outpass.service.AuthService;
import com.mit.outpass.service.OutpassService;

import jakarta.validation.Valid;

/**
 * REST Controller for student operations
 */
@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {
    
    @Autowired
    private OutpassService outpassService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Apply for new outpass
     */
    @PostMapping("/outpass")
    public ResponseEntity<ApiResponse<OutpassResponse>> applyOutpass(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody OutpassRequest request) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            OutpassResponse outpass = outpassService.applyOutpass(studentId, request);
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass application submitted successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Edit existing outpass
     */
    @PutMapping("/outpass/{id}")
    public ResponseEntity<ApiResponse<OutpassResponse>> editOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody OutpassRequest request) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            OutpassResponse outpass = outpassService.editOutpass(id, studentId, request);
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass updated successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Cancel outpass
     */
    @DeleteMapping("/outpass/{id}")
    public ResponseEntity<ApiResponse<String>> cancelOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            outpassService.cancelOutpass(id, studentId);
            
            ApiResponse<String> response = ApiResponse.success("Outpass cancelled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get student's outpasses
     */
    @GetMapping("/outpasses")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getMyOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            List<OutpassResponse> outpasses = outpassService.getStudentOutpasses(studentId);
            
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get specific outpass by ID
     */
    @GetMapping("/outpass/{id}")
    public ResponseEntity<ApiResponse<OutpassResponse>> getOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            OutpassResponse outpass = outpassService.getOutpassById(id);
            
            // Verify ownership
            if (!outpass.getStudentRollNumber().equals(authService.getAuthenticatedUser(token).getUsername())) {
                throw new IllegalArgumentException("You can only view your own outpass");
            }
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass retrieved successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get dashboard data for student
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardData>> getDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long studentId = authService.getUserIdFromToken(token);
            List<OutpassResponse> outpasses = outpassService.getStudentOutpasses(studentId);
            
            DashboardData dashboardData = new DashboardData();
            dashboardData.setTotalOutpasses(outpasses.size());
            dashboardData.setPendingOutpasses((int) outpasses.stream().filter(o -> o.getStatus().name().equals("PENDING")).count());
            dashboardData.setApprovedOutpasses((int) outpasses.stream().filter(o -> o.getStatus().name().equals("APPROVED")).count());
            dashboardData.setActiveOutpasses((int) outpasses.stream().filter(o -> o.getStatus().name().equals("ACTIVE")).count());
            dashboardData.setCompletedOutpasses((int) outpasses.stream().filter(o -> o.getStatus().name().equals("COMPLETED")).count());
            dashboardData.setRejectedOutpasses((int) outpasses.stream().filter(o -> o.getStatus().name().equals("REJECTED")).count());
            dashboardData.setRecentOutpasses(outpasses.stream().limit(5).toList());
            
            ApiResponse<DashboardData> response = ApiResponse.success("Dashboard data retrieved successfully", dashboardData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<DashboardData> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Inner class for dashboard data
     */
    public static class DashboardData {
        private int totalOutpasses;
        private int pendingOutpasses;
        private int approvedOutpasses;
        private int activeOutpasses;
        private int completedOutpasses;
        private int rejectedOutpasses;
        private List<OutpassResponse> recentOutpasses;
        
        // Getters and Setters
        public int getTotalOutpasses() { return totalOutpasses; }
        public void setTotalOutpasses(int totalOutpasses) { this.totalOutpasses = totalOutpasses; }
        
        public int getPendingOutpasses() { return pendingOutpasses; }
        public void setPendingOutpasses(int pendingOutpasses) { this.pendingOutpasses = pendingOutpasses; }
        
        public int getApprovedOutpasses() { return approvedOutpasses; }
        public void setApprovedOutpasses(int approvedOutpasses) { this.approvedOutpasses = approvedOutpasses; }
        
        public int getActiveOutpasses() { return activeOutpasses; }
        public void setActiveOutpasses(int activeOutpasses) { this.activeOutpasses = activeOutpasses; }
        
        public int getCompletedOutpasses() { return completedOutpasses; }
        public void setCompletedOutpasses(int completedOutpasses) { this.completedOutpasses = completedOutpasses; }
        
        public int getRejectedOutpasses() { return rejectedOutpasses; }
        public void setRejectedOutpasses(int rejectedOutpasses) { this.rejectedOutpasses = rejectedOutpasses; }
        
        public List<OutpassResponse> getRecentOutpasses() { return recentOutpasses; }
        public void setRecentOutpasses(List<OutpassResponse> recentOutpasses) { this.recentOutpasses = recentOutpasses; }
    }

    /**
     * Get authenticated student's profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<com.mit.outpass.dto.StudentProfileDto>> getProfile(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Student student = authService.getAuthenticatedStudent(token);

            // Map Student entity to DTO to avoid serializing relationships (prevent infinite recursion)
            com.mit.outpass.dto.StudentProfileDto dto = new com.mit.outpass.dto.StudentProfileDto();
            dto.setUserId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setFullName(student.getFullName());
            dto.setEmail(student.getEmail());
            dto.setMobileNumber(student.getMobileNumber());
            dto.setRollNumber(student.getRollNumber());
            dto.setHostelName(student.getHostelName());
            dto.setRoomNumber(student.getRoomNumber());
            dto.setCourse(student.getCourse());
            dto.setDegree(student.getDegree());
            dto.setYearOfStudy(student.getYearOfStudy());

            ApiResponse<com.mit.outpass.dto.StudentProfileDto> response = ApiResponse.success("Profile retrieved successfully", dto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<com.mit.outpass.dto.StudentProfileDto> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}