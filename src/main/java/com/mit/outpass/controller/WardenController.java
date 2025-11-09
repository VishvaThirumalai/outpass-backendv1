package com.mit.outpass.controller;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.OutpassResponse;
import com.mit.outpass.dto.ReviewRequest;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.enums.OutpassStatus;
import com.mit.outpass.service.AuthService;
import com.mit.outpass.service.OutpassService;
import com.mit.outpass.service.WardenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for warden operations with hostel-based filtering
 */
@RestController
@RequestMapping("/api/warden")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WardenController {
    
    @Autowired
    private OutpassService outpassService;
    
    @Autowired
    private WardenService wardenService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Get outpasses with hostel filtering and status filtering
     */
    @GetMapping("/outpasses")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getOutpasses(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String status) {
        try {
            // Remove "Bearer " prefix and get warden info
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            System.out.println("🏠 Warden " + wardenId + " accessing outpasses for hostel: " + hostelName + " with status: " + status);
            
            List<OutpassResponse> outpasses;
            if (status != null && !status.isEmpty()) {
                outpasses = wardenService.getOutpassesByHostelAndStatus(hostelName, status);
            } else {
                outpasses = wardenService.getAllOutpassesByHostel(hostelName);
            }
            
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get pending outpass applications for warden's hostel
     */
    @GetMapping("/outpasses/pending")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getPendingOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            List<OutpassResponse> outpasses = wardenService.getPendingOutpassesByHostel(hostelName);
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Pending outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get approved outpasses for warden's hostel
     */
    @GetMapping("/outpasses/approved")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getApprovedOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            List<OutpassResponse> outpasses = wardenService.getApprovedOutpassesByHostel(hostelName);
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Approved outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get active outpasses for warden's hostel
     */
    @GetMapping("/outpasses/active")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getActiveOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            List<OutpassResponse> outpasses = wardenService.getActiveOutpassesByHostel(hostelName);
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Active outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Review outpass (generic approve/reject) with hostel validation
     */
    @PutMapping("/outpass/{id}/review")
    public ResponseEntity<ApiResponse<OutpassResponse>> reviewOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            
            // Check if warden can review this outpass
            if (!wardenService.canWardenReviewOutpass(wardenId, id)) {
                ApiResponse<OutpassResponse> response = ApiResponse.error("Not authorized to review this outpass or outpass is not in pending status");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            
            OutpassResponse outpass = outpassService.reviewOutpass(id, wardenId, reviewRequest.getApproved(), reviewRequest.getComments());
            
            String action = reviewRequest.getApproved() ? "approved" : "rejected";
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass " + action + " successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Approve outpass with hostel validation
     */
    @PutMapping("/outpass/{id}/approve")
    public ResponseEntity<ApiResponse<OutpassResponse>> approveOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            
            // Check if warden can review this outpass
            if (!wardenService.canWardenReviewOutpass(wardenId, id)) {
                ApiResponse<OutpassResponse> response = ApiResponse.error("Not authorized to review this outpass or outpass is not in pending status");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            
            OutpassResponse outpass = outpassService.reviewOutpass(id, wardenId, true, reviewRequest.getComments());
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass approved successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Reject outpass with hostel validation
     */
    @PutMapping("/outpass/{id}/reject")
    public ResponseEntity<ApiResponse<OutpassResponse>> rejectOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            
            // Check if warden can review this outpass
            if (!wardenService.canWardenReviewOutpass(wardenId, id)) {
                ApiResponse<OutpassResponse> response = ApiResponse.error("Not authorized to review this outpass or outpass is not in pending status");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            
            OutpassResponse outpass = outpassService.reviewOutpass(id, wardenId, false, reviewRequest.getComments());
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass rejected successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get specific outpass details with hostel validation
     */
    @GetMapping("/outpass/{id}")
    public ResponseEntity<ApiResponse<OutpassResponse>> getOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String wardenHostel = wardenService.getWardenHostel(wardenId);
            
            OutpassResponse outpass = outpassService.getOutpassById(id);
            
            // Check if outpass belongs to warden's hostel
            if (!wardenHostel.equals(outpass.getHostelName())) {
                ApiResponse<OutpassResponse> response = ApiResponse.error("Not authorized to view this outpass");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass details retrieved successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get dashboard data for warden with hostel filtering
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            Map<String, Object> dashboardData = wardenService.getDetailedStatistics(hostelName);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success("Dashboard data retrieved successfully", dashboardData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get basic statistics for warden's hostel
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            String hostelName = wardenService.getWardenHostel(wardenId);
            
            Map<String, Long> stats = wardenService.getWardenStats(hostelName);
            
            ApiResponse<Map<String, Long>> response = ApiResponse.success("Statistics retrieved successfully", stats);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Map<String, Long>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get warden profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Warden>> getProfile(
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long wardenId = authService.getUserIdFromToken(token);
            Warden warden = wardenService.getWardenById(wardenId);
            
            ApiResponse<Warden> response = ApiResponse.success("Profile retrieved successfully", warden);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Warden> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}