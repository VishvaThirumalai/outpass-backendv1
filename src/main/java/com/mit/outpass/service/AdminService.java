package com.mit.outpass.service;

import com.mit.outpass.controller.AdminController;
import com.mit.outpass.controller.AuthController.RegisterRequest;
import com.mit.outpass.dto.UserDTO;
import com.mit.outpass.entity.Admin;
import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Student;
import com.mit.outpass.entity.User;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.entity.Security;
import com.mit.outpass.enums.UserRole;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.UserRepository;
import com.mit.outpass.repository.StudentRepository;
import com.mit.outpass.repository.WardenRepository;
import com.mit.outpass.repository.SecurityRepository;
import com.mit.outpass.repository.AdminRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mit.outpass.repository.OutpassRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private WardenRepository wardenRepository;
    
    @Autowired
    private SecurityRepository securityRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
     @Autowired
    private OutpassRepository outpassRepository;
    /**
     * Get dashboard statistics based on permission level
     */
    public Map<String, Object> getDashboardStatsByPermission(String permissionLevel) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            System.out.println("=== Getting Dashboard Stats for Permission: " + permissionLevel + " ===");
            
            // Get counts from repositories
            long students = userRepository.countByRole(UserRole.STUDENT);
            long wardens = userRepository.countByRole(UserRole.WARDEN);
            long security = userRepository.countByRole(UserRole.SECURITY);
            
            stats.put("students", students);
            stats.put("wardens", wardens);
            stats.put("security", security);
            
            // Only SUPER_ADMIN can see admin count
            if ("SUPER_ADMIN".equals(permissionLevel)) {
                long admins = userRepository.countByRole(UserRole.ADMIN);
                stats.put("admins", admins);
                stats.put("totalUsers", students + wardens + security + admins);
            } else {
                stats.put("totalUsers", students + wardens + security);
            }
            
            stats.put("success", true);
            stats.put("message", "Stats fetched successfully");
            stats.put("userPermissionLevel", permissionLevel);
            
        } catch (Exception e) {
            System.err.println("Error fetching dashboard stats: " + e.getMessage());
            e.printStackTrace();
            
            // Return default values
            stats.put("students", 0);
            stats.put("wardens", 0);
            stats.put("security", 0);
            stats.put("admins", 0);
            stats.put("totalUsers", 0);
            stats.put("success", false);
            stats.put("message", "Error: " + e.getMessage());
            stats.put("userPermissionLevel", permissionLevel);
        }
        
        return stats;
    }
    
    /**
     * Get current admin's permission level with proper database query
     */
    public String getAdminPermissionLevel(Long adminId) {
        try {
            System.out.println("🔍 Getting permission level for admin ID: " + adminId);
            
            // Use findById instead of findByUserId
            Optional<Admin> adminOpt = adminRepository.findById(adminId);
            
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                String dbPermissionLevel = admin.getPermissionLevel();
                
                System.out.println("🔍 Found admin: " + admin.getAdminId() + 
                    ", DB Permission: " + dbPermissionLevel);
                
                if (dbPermissionLevel == null) {
                    System.out.println("⚠️  Permission level is null, defaulting to MODERATOR");
                    return "MODERATOR";
                }
                
                String mappedPermission = mapPermissionLevel(dbPermissionLevel);
                System.out.println("✅ Mapped permission: " + dbPermissionLevel + " -> " + mappedPermission);
                
                return mappedPermission;
            } else {
                System.err.println("❌ Admin not found for ID: " + adminId);
                return "MODERATOR";
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting admin permission level for ID " + adminId + ": " + e.getMessage());
            e.printStackTrace();
            return "MODERATOR";
        }
    }
    
    /**
     * Map database permission levels to frontend expected values
     */
    private String mapPermissionLevel(String dbPermissionLevel) {
        if (dbPermissionLevel == null) {
            return "MODERATOR";
        }
        
        String upperLevel = dbPermissionLevel.toUpperCase().trim();
        
        switch (upperLevel) {
            case "SUPER_ADMIN":
            case "SUPERADMIN":
                return "SUPER_ADMIN";
            case "ADMIN":
            case "STANDARD": // Map STANDARD to ADMIN
                return "ADMIN";
            case "MODERATOR":
                return "MODERATOR";
            case "VIEWER":
                return "VIEWER";
            default:
                System.err.println("⚠️  Unknown permission level: " + dbPermissionLevel + ", defaulting to MODERATOR");
                return "MODERATOR"; // Default fallback
        }
    }

    /**
     * Get current admin's details with permission level
     */
    public Map<String, Object> getAdminDetails(Long adminId) {
        try {
            System.out.println("🔍 Getting admin details for ID: " + adminId);
            
            Optional<Admin> adminOpt = adminRepository.findById(adminId);
            
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                
                Map<String, Object> adminDetails = new HashMap<>();
                adminDetails.put("id", admin.getId()); // Inherited from User
                adminDetails.put("username", admin.getUsername()); // Inherited from User
                adminDetails.put("fullName", admin.getFullName()); // Inherited from User
                adminDetails.put("email", admin.getEmail()); // Inherited from User
                adminDetails.put("mobileNumber", admin.getMobileNumber()); // Inherited from User
                adminDetails.put("adminId", admin.getAdminId());
                adminDetails.put("department", admin.getDepartment());
                adminDetails.put("designation", admin.getDesignation());
                
                // Get mapped permission level
                String permissionLevel = getAdminPermissionLevel(adminId);
                adminDetails.put("permissionLevel", permissionLevel);
                adminDetails.put("dbPermissionLevel", admin.getPermissionLevel()); // For debugging
                
                adminDetails.put("isActive", admin.getIsActive()); // Inherited from User
                adminDetails.put("createdAt", admin.getCreatedAt()); // Inherited from User
                
                System.out.println("✅ Admin details: " + adminDetails);
                
                return adminDetails;
            } else {
                throw new ResourceNotFoundException("Admin", "id", adminId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting admin details: " + e.getMessage());
            e.printStackTrace();
            throw new ResourceNotFoundException("Admin", "id", adminId);
        }
    }
    
    /**
     * Get all students data
     */
    public List<UserDTO> getAllStudents() {
        try {
            List<Student> students = studentRepository.findAll();
            List<UserDTO> studentDTOs = new ArrayList<>();
            
            for (Student student : students) {
                UserDTO dto = convertToUserDTO(student);
                studentDTOs.add(dto);
            }
            
            System.out.println("Fetched " + studentDTOs.size() + " students");
            return studentDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all wardens data
     */
    public List<UserDTO> getAllWardens() {
        try {
            List<Warden> wardens = wardenRepository.findAll();
            List<UserDTO> wardenDTOs = new ArrayList<>();
            
            for (Warden warden : wardens) {
                UserDTO dto = convertToUserDTO(warden);
                wardenDTOs.add(dto);
            }
            
            System.out.println("Fetched " + wardenDTOs.size() + " wardens");
            return wardenDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching wardens: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all security personnel data
     */
    public List<UserDTO> getAllSecurity() {
        try {
            List<Security> securityList = securityRepository.findAll();
            List<UserDTO> securityDTOs = new ArrayList<>();
            
            for (Security security : securityList) {
                UserDTO dto = convertToUserDTO(security);
                securityDTOs.add(dto);
            }
            
            System.out.println("Fetched " + securityDTOs.size() + " security personnel");
            return securityDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching security: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all admins data - only for SUPER_ADMIN
     */
    public List<UserDTO> getAllAdmins() {
        try {
            List<Admin> admins = adminRepository.findAll();
            List<UserDTO> adminDTOs = new ArrayList<>();
            
            for (Admin admin : admins) {
                UserDTO dto = convertToUserDTO(admin);
                adminDTOs.add(dto);
            }
            
            System.out.println("Fetched " + adminDTOs.size() + " admins");
            return adminDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching admins: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Register user by admin (only admin can register users)
     */
    public void registerUserByAdmin(String adminToken, RegisterRequest registerRequest) {
        // Verify admin access
        Claims claims = authService.validateToken(adminToken);
        String username = claims.getSubject();
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        
        if (role != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only administrators can register users");
        }
        
        // Use existing auth service to register user
        authService.registerUser(registerRequest);
    }
    
    /**
     * Get user by ID - FIXED VERSION
     */
    public UserDTO getUserById(Long userId) {
        try {
            System.out.println("🔍 Getting user by ID: " + userId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found with ID: " + userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            
            System.out.println("✅ Found user: " + user.getUsername() + " with role: " + user.getRole());
            return convertToUserDTO(user);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting user by ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }
    
    /**
     * Delete user by ID - FIXED VERSION
     */
  @Transactional
public Map<String, Object> deleteUser(Long userId) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        System.out.println("🔄 Deleting user ID: " + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        String username = user.getUsername();
        UserRole role = user.getRole();
        
        System.out.println("🗑️ Deleting " + role + ": " + username);
        
        // USE NATIVE QUERIES TO CONTROL DELETION ORDER
        switch (role) {
            case STUDENT:
                // Delete student using native query first
                studentRepository.deleteStudentByIdNative(userId);
                System.out.println("✅ Student entity deleted via native query");
                break;
                
            case WARDEN:
                // Delete warden using native query first
                wardenRepository.deleteWardenByIdNative(userId);
                System.out.println("✅ Warden entity deleted via native query");
                break;
                
            case SECURITY:
                // Delete security using native query first
                securityRepository.deleteSecurityByIdNative(userId);
                System.out.println("✅ Security entity deleted via native query");
                break;
                
            case ADMIN:
                // Delete admin using native query first
                adminRepository.deleteAdminByIdNative(userId);
                System.out.println("✅ Admin entity deleted via native query");
                break;
        }
        
        // Then delete user using native query
        userRepository.deleteUserByIdNative(userId);
        System.out.println("✅ User deleted via native query: " + username);
        
        response.put("success", true);
        response.put("message", "User deleted successfully");
        response.put("deletedUserId", userId);
        response.put("deletedUsername", username);
        
    } catch (Exception e) {
        System.err.println("❌ Error deleting user: " + e.getMessage());
        
        response.put("success", false);
        response.put("message", "Cannot delete user. They have related records in the system.");
        response.put("error", e.getMessage());
    }
    
    return response;
}
    
    /**
     * Update user by ID - FIXED VERSION
     */
    @Transactional
    public UserDTO updateUser(Long userId, AdminController.UserUpdateRequest updateRequest) {
        try {
            System.out.println("🔄 Updating user ID: " + userId + " with data: " + updateRequest);
            
            // Check if user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found for update with ID: " + userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            
            System.out.println("✅ Found user to update: " + user.getUsername() + " with role: " + user.getRole());
            
            // Update common user fields
            if (updateRequest.getFullName() != null && !updateRequest.getFullName().trim().isEmpty()) {
                user.setFullName(updateRequest.getFullName().trim());
                System.out.println("✅ Updated full name: " + updateRequest.getFullName());
            }
            
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
                // Check if email is already taken by another user
                if (userRepository.existsByEmailAndIdNot(updateRequest.getEmail().trim(), userId)) {
                    throw new IllegalArgumentException("Email is already taken by another user");
                }
                user.setEmail(updateRequest.getEmail().trim());
                System.out.println("✅ Updated email: " + updateRequest.getEmail());
            }
            
            if (updateRequest.getMobileNumber() != null && !updateRequest.getMobileNumber().trim().isEmpty()) {
                user.setMobileNumber(updateRequest.getMobileNumber().trim());
                System.out.println("✅ Updated mobile number: " + updateRequest.getMobileNumber());
            }
            
            // Save user first to ensure base entity is updated
            User updatedUser = userRepository.save(user);
            System.out.println("✅ Base user updated: " + updatedUser.getUsername());
            
            // Update role-specific fields
            switch (user.getRole()) {
                case STUDENT:
                    updateStudentFields(user, updateRequest);
                    break;
                case WARDEN:
                    updateWardenFields(user, updateRequest);
                    break;
                case SECURITY:
                    updateSecurityFields(user, updateRequest);
                    break;
                case ADMIN:
                    updateAdminFields(user, updateRequest);
                    break;
                default:
                    System.out.println("⚠️ No specific fields to update for role: " + user.getRole());
                    break;
            }
            
            // Fetch the updated user with all role-specific data
            User finalUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            System.out.println("✅ User completely updated: " + finalUser.getUsername());
            
            return convertToUserDTO(finalUser);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ User not found during update: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }
    
    /**
     * Toggle user active status - FIXED VERSION
     */
    @Transactional
    public UserDTO toggleUserStatus(Long userId, boolean active) {
        try {
            System.out.println("🔄 Toggling user status - ID: " + userId + ", Active: " + active);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found for status toggle with ID: " + userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            
            System.out.println("✅ Found user for status toggle: " + user.getUsername() + ", current status: " + user.getIsActive());
            
            user.setIsActive(active);
            User updatedUser = userRepository.save(user);
            
            System.out.println("✅ User status updated: " + updatedUser.getUsername() + " -> " + (active ? "Active" : "Inactive"));
            
            return convertToUserDTO(updatedUser);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ User not found during status toggle: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error toggling user status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to toggle user status: " + e.getMessage());
        }
    }
    
    /**
     * Update student-specific fields
     */
    private void updateStudentFields(User user, AdminController.UserUpdateRequest updateRequest) {
        try {
            Student student = studentRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "user_id", user.getId()));
            
            System.out.println("🔄 Updating student fields for: " + student.getUsername());
            
            if (updateRequest.getCourse() != null) {
                student.setCourse(updateRequest.getCourse());
                System.out.println("✅ Updated course: " + updateRequest.getCourse());
            }
            if (updateRequest.getDegree() != null) {
                student.setDegree(updateRequest.getDegree());
                System.out.println("✅ Updated degree: " + updateRequest.getDegree());
            }
            if (updateRequest.getYearOfStudy() != null) {
                student.setYearOfStudy(updateRequest.getYearOfStudy());
                System.out.println("✅ Updated year of study: " + updateRequest.getYearOfStudy());
            }
            if (updateRequest.getHostelName() != null) {
                student.setHostelName(updateRequest.getHostelName());
                System.out.println("✅ Updated hostel name: " + updateRequest.getHostelName());
            }
            if (updateRequest.getRoomNumber() != null) {
                student.setRoomNumber(updateRequest.getRoomNumber());
                System.out.println("✅ Updated room number: " + updateRequest.getRoomNumber());
            }
            
            Student savedStudent = studentRepository.save(student);
            System.out.println("✅ Student fields saved: " + savedStudent.getUsername());
            
        } catch (Exception e) {
            System.err.println("❌ Error updating student fields: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update student fields: " + e.getMessage());
        }
    }
    
    /**
     * Update warden-specific fields
     */
    private void updateWardenFields(User user, AdminController.UserUpdateRequest updateRequest) {
        try {
            Warden warden = wardenRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warden", "user_id", user.getId()));
            
            System.out.println("🔄 Updating warden fields for: " + warden.getUsername());
            
            if (updateRequest.getDepartment() != null) {
                warden.setDepartment(updateRequest.getDepartment());
                System.out.println("✅ Updated department: " + updateRequest.getDepartment());
            }
            if (updateRequest.getDesignation() != null) {
                warden.setDesignation(updateRequest.getDesignation());
                System.out.println("✅ Updated designation: " + updateRequest.getDesignation());
            }
            if (updateRequest.getHostelAssigned() != null) {
                warden.setHostelAssigned(updateRequest.getHostelAssigned());
                System.out.println("✅ Updated hostel assigned: " + updateRequest.getHostelAssigned());
            }
            
            Warden savedWarden = wardenRepository.save(warden);
            System.out.println("✅ Warden fields saved: " + savedWarden.getUsername());
            
        } catch (Exception e) {
            System.err.println("❌ Error updating warden fields: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update warden fields: " + e.getMessage());
        }
    }
    
    /**
     * Update security-specific fields
     */
    private void updateSecurityFields(User user, AdminController.UserUpdateRequest updateRequest) {
        try {
            Security security = securityRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Security", "user_id", user.getId()));
            
            System.out.println("🔄 Updating security fields for: " + security.getUsername());
            
            if (updateRequest.getShift() != null) {
                security.setShift(updateRequest.getShift());
                System.out.println("✅ Updated shift: " + updateRequest.getShift());
            }
            if (updateRequest.getGateAssigned() != null) {
                security.setGateAssigned(updateRequest.getGateAssigned());
                System.out.println("✅ Updated gate assigned: " + updateRequest.getGateAssigned());
            }
            
            Security savedSecurity = securityRepository.save(security);
            System.out.println("✅ Security fields saved: " + savedSecurity.getUsername());
            
        } catch (Exception e) {
            System.err.println("❌ Error updating security fields: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update security fields: " + e.getMessage());
        }
    }
    
    /**
     * Update admin-specific fields - Fixed version
     */
    private void updateAdminFields(User user, AdminController.UserUpdateRequest updateRequest) {
        try {
            Admin admin = adminRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin", "user_id", user.getId()));
            
            System.out.println("🔄 Updating admin fields for: " + admin.getUsername());
            
            if (updateRequest.getAdminId() != null) {
                admin.setAdminId(updateRequest.getAdminId());
                System.out.println("✅ Updated admin ID: " + updateRequest.getAdminId());
            }
            if (updateRequest.getDepartment() != null) {
                admin.setDepartment(updateRequest.getDepartment());
                System.out.println("✅ Updated department: " + updateRequest.getDepartment());
            }
            if (updateRequest.getDesignation() != null) {
                admin.setDesignation(updateRequest.getDesignation());
                System.out.println("✅ Updated designation: " + updateRequest.getDesignation());
            }
            if (updateRequest.getPermissionLevel() != null) {
                admin.setPermissionLevel(updateRequest.getPermissionLevel());
                System.out.println("✅ Updated permission level: " + updateRequest.getPermissionLevel());
            }
            
            Admin savedAdmin = adminRepository.save(admin);
            System.out.println("✅ Admin fields saved: " + savedAdmin.getUsername());
            
        } catch (Exception e) {
            System.err.println("❌ Error updating admin fields: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update admin fields: " + e.getMessage());
        }
    }

    /**
     * Verify user identity for password reset
     */
    public boolean verifyUserIdentity(String username, String mobileNumber) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return false;
            }
            
            User user = userOptional.get();
            return user.getMobileNumber() != null && user.getMobileNumber().equals(mobileNumber);
        } catch (Exception e) {
            System.err.println("Error verifying user identity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert User entity to UserDTO with proper permission level mapping
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setRole(user.getRole());
        dto.setActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A");
        
        // Add role-specific fields
        switch (user.getRole()) {
            case STUDENT:
                Student student = studentRepository.findById(user.getId()).orElse(null);
                if (student != null) {
                    dto.setRollNumber(student.getRollNumber());
                    dto.setCourse(student.getCourse());
                    dto.setDegree(student.getDegree());
                    dto.setYearOfStudy(student.getYearOfStudy());
                    dto.setHostelName(student.getHostelName());
                    dto.setRoomNumber(student.getRoomNumber());
                }
                break;
            case WARDEN:
                Warden warden = wardenRepository.findById(user.getId()).orElse(null);
                if (warden != null) {
                    dto.setEmployeeId(warden.getEmployeeId());
                    dto.setDepartment(warden.getDepartment());
                    dto.setDesignation(warden.getDesignation());
                    dto.setHostelAssigned(warden.getHostelAssigned());
                }
                break;
            case SECURITY:
                Security security = securityRepository.findById(user.getId()).orElse(null);
                if (security != null) {
                    dto.setSecurityId(security.getSecurityId());
                    dto.setShift(security.getShift());
                    dto.setGateAssigned(security.getGateAssigned());
                }
                break;
            case ADMIN:
                // Since Admin extends User, we can cast or find by ID
                Admin admin = adminRepository.findById(user.getId()).orElse(null);
                if (admin != null) {
                    dto.setAdminId(admin.getAdminId());
                    dto.setDepartment(admin.getDepartment());
                    dto.setDesignation(admin.getDesignation());
                    
                    // Map permission levels for frontend display
                    String dbPermissionLevel = admin.getPermissionLevel();
                    String frontendPermissionLevel = mapPermissionLevel(dbPermissionLevel);
                    dto.setPermissionLevel(frontendPermissionLevel);
                    
                    System.out.println("Admin " + user.getUsername() + 
                        " - DB Permission: " + dbPermissionLevel + 
                        " -> Frontend: " + frontendPermissionLevel);
                }
                break;
        }
        
        return dto;
    }
}