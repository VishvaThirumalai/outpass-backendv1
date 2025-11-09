package com.mit.outpass.controller;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.UserDTO;
import com.mit.outpass.entity.User;
import com.mit.outpass.service.AuthService;
import com.mit.outpass.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile operations (non-admin users)
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Get authenticated user from token
            User user = authService.getAuthenticatedUser(token);
            UserDTO userProfile = userService.getUserProfile(user.getId());
            
            ApiResponse<UserDTO> response = ApiResponse.success("Profile fetched successfully", userProfile);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Error fetching profile: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUserProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserProfileUpdateRequest updateRequest) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Get authenticated user from token
            User user = authService.getAuthenticatedUser(token);
            
            // Users can only update their own profile
            UserDTO updatedUser = userService.updateUserProfile(user.getId(), updateRequest);
            
            ApiResponse<UserDTO> response = ApiResponse.success("Profile updated successfully", updatedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Error updating profile: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * DTO for user profile update requests
     */
    public static class UserProfileUpdateRequest {
        private String fullName;
        private String email;
        private String mobileNumber;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    }
}