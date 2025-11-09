package com.mit.outpass.controller;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.UserDTO;
import com.mit.outpass.entity.Admin;
import com.mit.outpass.entity.User;
import com.mit.outpass.enums.UserRole;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.service.AdminService;
import com.mit.outpass.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Get dashboard statistics with permission check
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Verify admin access and get permission level
            Claims claims = authService.validateToken(token);
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access dashboard stats"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            Map<String, Object> stats = adminService.getDashboardStatsByPermission(permissionLevel);
            
            // Log for debugging
            System.out.println("📊 Dashboard stats for " + username + " with permission: " + permissionLevel);
            System.out.println("📊 Stats data: " + stats);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success("Stats fetched successfully", stats);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("❌ Error fetching dashboard stats: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error fetching stats: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get current admin's permission level and details
     */
    @GetMapping("/my-permission")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyPermissionLevel(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Verify admin access
            Claims claims = authService.validateToken(token);
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access this endpoint"));
            }
            
            // Get admin user
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            Map<String, Object> adminDetails = adminService.getAdminDetails(adminUser.getId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("permissionLevel", permissionLevel);
            responseData.put("username", username);
            responseData.put("role", role.toString());
            responseData.put("adminDetails", adminDetails);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success("Permission level fetched", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error fetching permission level: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all students data
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllStudents(
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access student data"));
            }
            
            List<UserDTO> students = adminService.getAllStudents();
            ApiResponse<List<UserDTO>> response = ApiResponse.success("Students fetched successfully", students);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<UserDTO>> response = ApiResponse.error("Error fetching students: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all wardens data
     */
    @GetMapping("/wardens")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllWardens(
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access warden data"));
            }
            
            List<UserDTO> wardens = adminService.getAllWardens();
            ApiResponse<List<UserDTO>> response = ApiResponse.success("Wardens fetched successfully", wardens);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<UserDTO>> response = ApiResponse.error("Error fetching wardens: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all security personnel data
     */
    @GetMapping("/security")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllSecurity(
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access security data"));
            }
            
            List<UserDTO> security = adminService.getAllSecurity();
            ApiResponse<List<UserDTO>> response = ApiResponse.success("Security personnel fetched successfully", security);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<UserDTO>> response = ApiResponse.error("Error fetching security: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all admins data - only for SUPER_ADMIN
     */
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllAdmins(
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access and get permission level
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access admin data"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            // Only SUPER_ADMIN can access admin data
            if (!"SUPER_ADMIN".equals(permissionLevel)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required."));
            }
            
            List<UserDTO> admins = adminService.getAllAdmins();
            
            // Debug log
            System.out.println("Fetched " + admins.size() + " admins with permissions:");
            for (UserDTO admin : admins) {
                System.out.println("Admin: " + admin.getUsername() + " - Permission: " + admin.getPermissionLevel());
            }
            
            ApiResponse<List<UserDTO>> response = ApiResponse.success("Admins fetched successfully", admins);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching admins: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<List<UserDTO>> response = ApiResponse.error("Error fetching admins: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Create new admin - only for SUPER_ADMIN
     */
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<String>> createAdmin(
            @RequestBody RegisterAdminRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access and check permission level
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can create other admins"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            // Only SUPER_ADMIN can create other admins
            if (!"SUPER_ADMIN".equals(permissionLevel)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required to create admins."));
            }
            
            // Create registration request
            AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
            registerRequest.setUsername(generateUsername(request.getFullName(), "ADM"));
            registerRequest.setPassword(generateTempPassword());
            registerRequest.setFullName(request.getFullName());
            registerRequest.setEmail(request.getEmail());
            registerRequest.setMobileNumber(request.getMobileNumber());
            registerRequest.setRole("ADMIN");
            registerRequest.setDepartment(request.getDepartment());
            registerRequest.setDesignation(request.getDesignation());
            registerRequest.setPermissionLevel(request.getPermissionLevel());
            
            authService.registerUser(registerRequest);
            
            ApiResponse<String> response = ApiResponse.success("Admin created successfully with temporary password");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Admin creation failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update admin by ID - only for SUPER_ADMIN
     */
    @PutMapping("/admins/{adminId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateAdmin(
            @PathVariable Long adminId,
            @RequestBody AdminUpdateRequest updateRequest,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access and check permission level
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can update admins"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            // Only SUPER_ADMIN can update admins
            if (!"SUPER_ADMIN".equals(permissionLevel)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required to update admins."));
            }
            
            // Convert to UserUpdateRequest for the service
            UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
            userUpdateRequest.setFullName(updateRequest.getFullName());
            userUpdateRequest.setEmail(updateRequest.getEmail());
            userUpdateRequest.setMobileNumber(updateRequest.getMobileNumber());
            userUpdateRequest.setDepartment(updateRequest.getDepartment());
            userUpdateRequest.setDesignation(updateRequest.getDesignation());
            userUpdateRequest.setAdminId(updateRequest.getAdminId());
            userUpdateRequest.setPermissionLevel(updateRequest.getPermissionLevel());
            
            UserDTO updatedAdmin = adminService.updateUser(adminId, userUpdateRequest);
            
            ApiResponse<UserDTO> response = ApiResponse.success("Admin updated successfully", updatedAdmin);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Error updating admin: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete admin by ID - only for SUPER_ADMIN
     */
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(
            @PathVariable Long adminId,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access and check permission level
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can delete admins"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            // Only SUPER_ADMIN can delete admins
            if (!"SUPER_ADMIN".equals(permissionLevel)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required to delete admins."));
            }
            
            // Prevent self-deletion
            if (adminId.equals(adminUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("You cannot delete your own account"));
            }
            
            adminService.deleteUser(adminId);
            
            ApiResponse<String> response = ApiResponse.success("Admin deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Error deleting admin: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Toggle admin status - only for SUPER_ADMIN
     */
    @PatchMapping("/admins/{adminId}/status")
    public ResponseEntity<ApiResponse<UserDTO>> toggleAdminStatus(
            @PathVariable Long adminId,
            @RequestBody StatusUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access and check permission level
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can toggle admin status"));
            }
            
            // Get admin user to check permission level
            String username = claims.getSubject();
            User adminUser = authService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
            
            // Only SUPER_ADMIN can toggle admin status
            if (!"SUPER_ADMIN".equals(permissionLevel)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required to toggle admin status."));
            }
            
            // Prevent self-deactivation
            if (adminId.equals(adminUser.getId()) && !request.isActive()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("You cannot deactivate your own account"));
            }
            
            UserDTO updatedAdmin = adminService.toggleUserStatus(adminId, request.isActive());
            
            ApiResponse<UserDTO> response = ApiResponse.success("Admin status updated successfully", updatedAdmin);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Error updating admin status: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Delete user by ID - FIXED RESPONSE FORMAT
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can delete users"));
            }
            
            Map<String, Object> deleteResult = adminService.deleteUser(userId);
            
            if (Boolean.TRUE.equals(deleteResult.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    (String) deleteResult.get("message"), 
                    deleteResult
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    (String) deleteResult.get("message")
                );
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error deleting user: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update user by ID - FIXED RESPONSE FORMAT
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest updateRequest,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can update users"));
            }
            
            System.out.println("🔄 Updating user ID: " + userId);
            
            UserDTO updatedUser = adminService.updateUser(userId, updateRequest);
            
            ApiResponse<UserDTO> response = ApiResponse.success("User updated successfully", updatedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ User not found during update: " + e.getMessage());
            ApiResponse<UserDTO> response = ApiResponse.error("User not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<UserDTO> response = ApiResponse.error("Error updating user: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Toggle user status - FIXED RESPONSE FORMAT
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can toggle user status"));
            }
            
            System.out.println("🔄 Toggling user status for ID: " + userId + " to: " + request.isActive());
            
            UserDTO updatedUser = adminService.toggleUserStatus(userId, request.isActive());
            
            ApiResponse<UserDTO> response = ApiResponse.success("User status updated successfully", updatedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ User not found during status toggle: " + e.getMessage());
            ApiResponse<UserDTO> response = ApiResponse.error("User not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("❌ Error updating user status: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<UserDTO> response = ApiResponse.error("Error updating user status: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get user by ID - FIXED RESPONSE FORMAT
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can access user data"));
            }
            
            System.out.println("🔍 Fetching user by ID: " + userId);
            
            UserDTO user = adminService.getUserById(userId);
            
            ApiResponse<UserDTO> response = ApiResponse.success("User fetched successfully", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ User not found: " + e.getMessage());
            ApiResponse<UserDTO> response = ApiResponse.error("User not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("❌ Error fetching user: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<UserDTO> response = ApiResponse.error("Error fetching user: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Simple password reset for all user types
     */
    @PostMapping("/simple-password-reset")
    public ResponseEntity<ApiResponse<String>> simplePasswordReset(
            @RequestBody SimplePasswordResetRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            Claims claims = authService.validateToken(token.replace("Bearer ", ""));
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            if (role != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only administrators can reset passwords"));
            }
            
            // Verify user identity
            boolean identityVerified = authService.verifyUserIdentity(request.getUsername(), request.getMobileNumber());
            if (!identityVerified) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username and mobile number do not match"));
            }
            
            // Reset password
            authService.resetPassword(request.getUsername(), request.getNewPassword());
            
            ApiResponse<String> response = ApiResponse.success("Password reset successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Password reset failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    
// Update registerStudent method
@PostMapping("/register/student")
public ResponseEntity<ApiResponse<String>> registerStudent(
        @RequestBody RegisterStudentRequest request,
        @RequestHeader("Authorization") String token) {
    try {
        // Verify admin access
        Claims claims = authService.validateToken(token.replace("Bearer ", ""));
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        
        if (role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only administrators can register students"));
        }
        
        // Create registration request with ALL fields
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername(generateUsername(request.getFullName(), "STU"));
        registerRequest.setPassword(generateTempPassword());
        registerRequest.setFullName(request.getFullName());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setMobileNumber(request.getMobileNumber());
        registerRequest.setRole("STUDENT");
        registerRequest.setRollNumber(request.getRollNumber());
        registerRequest.setCourse(request.getCourse());
        registerRequest.setDegree(request.getDegree());
        registerRequest.setYearOfStudy(request.getYearOfStudy());
        registerRequest.setHostelName(request.getHostelName());
        registerRequest.setRoomNumber(request.getRoomNumber());
        registerRequest.setAddress(request.getAddress());
        registerRequest.setGuardianName(request.getGuardianName());
        registerRequest.setGuardianMobile(request.getGuardianMobile());
        registerRequest.setGuardianRelation(request.getGuardianRelation());
        
        authService.registerUser(registerRequest);
        
        ApiResponse<String> response = ApiResponse.success("Student registered successfully with temporary password");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
        ApiResponse<String> response = ApiResponse.error("Student registration failed: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

// Update registerWarden method
@PostMapping("/register/warden")
public ResponseEntity<ApiResponse<String>> registerWarden(
        @RequestBody RegisterWardenRequest request,
        @RequestHeader("Authorization") String token) {
    try {
        // Verify admin access
        Claims claims = authService.validateToken(token.replace("Bearer ", ""));
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        
        if (role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only administrators can register wardens"));
        }
        
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername(generateUsername(request.getFullName(), "WRD"));
        registerRequest.setPassword(generateTempPassword());
        registerRequest.setFullName(request.getFullName());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setMobileNumber(request.getMobileNumber());
        registerRequest.setRole("WARDEN");
        registerRequest.setEmployeeId(request.getEmployeeId());
        registerRequest.setDepartment(request.getDepartment());
        registerRequest.setDesignation(request.getDesignation());
        registerRequest.setHostelAssigned(request.getHostelAssigned());
        registerRequest.setYearsOfExperience(request.getYearsOfExperience());
        registerRequest.setOfficeLocation(request.getOfficeLocation());
        registerRequest.setOfficeHours(request.getOfficeHours());
        
        authService.registerUser(registerRequest);
        
        ApiResponse<String> response = ApiResponse.success("Warden registered successfully with temporary password");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
        ApiResponse<String> response = ApiResponse.error("Warden registration failed: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

// Update registerSecurity method
@PostMapping("/register/security")
public ResponseEntity<ApiResponse<String>> registerSecurity(
        @RequestBody RegisterSecurityRequest request,
        @RequestHeader("Authorization") String token) {
    try {
        // Verify admin access
        Claims claims = authService.validateToken(token.replace("Bearer ", ""));
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        
        if (role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only administrators can register security personnel"));
        }
        
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername(generateUsername(request.getFullName(), "SEC"));
        registerRequest.setPassword(generateTempPassword());
        registerRequest.setFullName(request.getFullName());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setMobileNumber(request.getMobileNumber());
        registerRequest.setRole("SECURITY");
        registerRequest.setSecurityId(request.getSecurityId());
        registerRequest.setShift(request.getShift());
        registerRequest.setGateAssigned(request.getGateAssigned());
        registerRequest.setSupervisorName(request.getSupervisorName());
        registerRequest.setSupervisorContact(request.getSupervisorContact());
        registerRequest.setYearsOfService(request.getYearsOfService());
        registerRequest.setSecurityClearanceLevel(request.getSecurityClearanceLevel());
        
        authService.registerUser(registerRequest);
        
        ApiResponse<String> response = ApiResponse.success("Security personnel registered successfully with temporary password");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
        ApiResponse<String> response = ApiResponse.error("Security registration failed: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

// Update registerAdmin method
@PostMapping("/register/admin")
public ResponseEntity<ApiResponse<String>> registerAdmin(
        @RequestBody RegisterAdminRequest request,
        @RequestHeader("Authorization") String token) {
    try {
        // Verify admin access and check permission level
        Claims claims = authService.validateToken(token.replace("Bearer ", ""));
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        
        if (role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only administrators can register other admins"));
        }
        
        // Get admin user to check permission level
        String username = claims.getSubject();
        User adminUser = authService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        String permissionLevel = adminService.getAdminPermissionLevel(adminUser.getId());
        
        // Only SUPER_ADMIN can register other admins
        if (!"SUPER_ADMIN".equals(permissionLevel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. SUPER_ADMIN permission required to register admins."));
        }
        
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername(generateUsername(request.getFullName(), "ADM"));
        registerRequest.setPassword(generateTempPassword());
        registerRequest.setFullName(request.getFullName());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setMobileNumber(request.getMobileNumber());
        registerRequest.setRole("ADMIN");
        registerRequest.setDepartment(request.getDepartment());
        registerRequest.setDesignation(request.getDesignation());
        registerRequest.setPermissionLevel(request.getPermissionLevel());
        registerRequest.setAdminId(request.getAdminId());
        
        authService.registerUser(registerRequest);
        
        ApiResponse<String> response = ApiResponse.success("Admin registered successfully with temporary password");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
        ApiResponse<String> response = ApiResponse.error("Admin registration failed: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
    // Helper methods
    private String generateUsername(String fullName, String prefix) {
        String baseName = fullName.replaceAll("\\s+", "").toLowerCase();
        return prefix + "_" + baseName + "_" + System.currentTimeMillis();
    }
    
    private String generateTempPassword() {
        return "TempPass123!";
    }
    
    // DTO classes
    
    public static class UserUpdateRequest {
        private String fullName;
        private String email;
        private String mobileNumber;
        private String course;
        private String degree;
        private Integer yearOfStudy;
        private String hostelName;
        private String roomNumber;
        private String department;
        private String designation;
        private String hostelAssigned;
        private String shift;
        private String gateAssigned;
        private String adminId;
        private String permissionLevel;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        
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
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        
        public String getHostelAssigned() { return hostelAssigned; }
        public void setHostelAssigned(String hostelAssigned) { this.hostelAssigned = hostelAssigned; }
        
        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }
        
        public String getGateAssigned() { return gateAssigned; }
        public void setGateAssigned(String gateAssigned) { this.gateAssigned = gateAssigned; }
        
        public String getAdminId() { return adminId; }
        public void setAdminId(String adminId) { this.adminId = adminId; }
        
        public String getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    }
    
    public static class AdminUpdateRequest {
        private String fullName;
        private String email;
        private String mobileNumber;
        private String department;
        private String designation;
        private String adminId;
        private String permissionLevel;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        
        public String getAdminId() { return adminId; }
        public void setAdminId(String adminId) { this.adminId = adminId; }
        
        public String getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    }
    
    public static class StatusUpdateRequest {
        private boolean active;
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
    
    public static class SimplePasswordResetRequest {
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
    

public static class RegisterStudentRequest {
    private String fullName;
    private String email;
    private String mobileNumber;
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
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
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
}

public static class RegisterWardenRequest {
    private String fullName;
    private String email;
    private String mobileNumber;
    private String employeeId;
    private String department;
    private String designation;
    private String hostelAssigned;
    private Integer yearsOfExperience;
    private String officeLocation;
    private String officeHours;
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
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
}

public static class RegisterSecurityRequest {
    private String fullName;
    private String email;
    private String mobileNumber;
    private String securityId;
    private String shift;
    private String gateAssigned;
    private String supervisorName;
    private String supervisorContact;
    private Integer yearsOfService;
    private String securityClearanceLevel;
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
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
}

public static class RegisterAdminRequest {
    private String fullName;
    private String email;
    private String mobileNumber;
    private String department;
    private String designation;
    private String permissionLevel;
    private String adminId;
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}
}