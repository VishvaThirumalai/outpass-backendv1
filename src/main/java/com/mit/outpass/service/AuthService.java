package com.mit.outpass.service;

import com.mit.outpass.controller.AuthController.RegisterRequest;
import com.mit.outpass.dto.LoginRequest;
import com.mit.outpass.dto.LoginResponse;
import com.mit.outpass.entity.Admin;
import com.mit.outpass.entity.Security;
import com.mit.outpass.entity.Student;
import com.mit.outpass.entity.User;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.enums.UserRole;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.AdminRepository;
import com.mit.outpass.repository.SecurityRepository;
import com.mit.outpass.repository.StudentRepository;
import com.mit.outpass.repository.UserRepository;
import com.mit.outpass.repository.WardenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    
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
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        System.out.println("🔐 AuthService: Attempting login for user: " + loginRequest.getUsername());
        
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isEmpty()) {
            System.out.println("❌ User not found: " + loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
        
        User user = userOptional.get();
        System.out.println("✅ User found: " + user.getUsername() + ", Role: " + user.getRole());
        
        // DEBUG: Check password format
        System.out.println("🔑 Stored password length: " + user.getPassword().length());
        System.out.println("🔑 Stored password starts with: " + (user.getPassword().length() > 10 ? user.getPassword().substring(0, 10) : user.getPassword()));
        
        // Use PasswordEncoder for password verification
        boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        
        System.out.println("🔑 Password verification result: " + passwordMatches);
        
        if (!passwordMatches) {
            // Additional debug: try direct comparison for troubleshooting
            System.out.println("🔑 Direct comparison: " + loginRequest.getPassword().equals(user.getPassword()));
            throw new BadCredentialsException("Invalid username or password");
        }
        
        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated. Please contact administrator.");
        }
        
        // Role verification (optional)
        if (loginRequest.getRole() != null && !loginRequest.getRole().isEmpty()) {
            try {
                UserRole requestedRole = UserRole.valueOf(loginRequest.getRole().toUpperCase());
                if (!user.getRole().equals(requestedRole)) {
                    System.out.println("❌ Role mismatch. User role: " + user.getRole() + ", Requested: " + requestedRole);
                    throw new BadCredentialsException("Invalid role for this user");
                }
            } catch (IllegalArgumentException e) {
                throw new BadCredentialsException("Invalid role specified");
            }
        }
        
        String token = generateJwtToken(user);
        System.out.println("✅ JWT Token generated successfully");
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getFullName(), 
                                user.getRole(), user.getId());
        System.out.println("✅ Login successful for: " + response.getUsername() + ", Role: " + response.getRole());
        
        return response;
    }
    
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        System.out.println("🔐 Starting registration for: " + registerRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Validate role-specific fields
        UserRole role = UserRole.valueOf(registerRequest.getRole().toUpperCase());
        
        switch (role) {
            case STUDENT:
                if (registerRequest.getRollNumber() == null || registerRequest.getRollNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("Roll number is required for student");
                }
                if (studentRepository.existsByRollNumber(registerRequest.getRollNumber())) {
                    throw new IllegalArgumentException("Roll number already exists");
                }
                createStudent(registerRequest);
                break;
                
            case WARDEN:
                if (registerRequest.getEmployeeId() == null || registerRequest.getEmployeeId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Employee ID is required for warden");
                }
                if (wardenRepository.existsByEmployeeId(registerRequest.getEmployeeId())) {
                    throw new IllegalArgumentException("Employee ID already exists");
                }
                createWarden(registerRequest);
                break;
                
            case SECURITY:
                if (registerRequest.getSecurityId() == null || registerRequest.getSecurityId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Security ID is required for security personnel");
                }
                if (securityRepository.existsBySecurityId(registerRequest.getSecurityId())) {
                    throw new IllegalArgumentException("Security ID already exists");
                }
                createSecurity(registerRequest);
                break;
                
            case ADMIN:
                // For admin, we'll generate the admin ID automatically
                createAdmin(registerRequest);
                break;
                
            default:
                throw new IllegalArgumentException("Invalid role specified");
        }
        
        System.out.println("✅ Registration completed successfully for: " + registerRequest.getUsername());
    }
    
    private void createStudent(RegisterRequest request) {
        Student student = new Student();
        setCommonUserFields(student, request);
        student.setRollNumber(request.getRollNumber());
        student.setCourse(request.getCourse());
        student.setDegree(request.getDegree());
        student.setYearOfStudy(request.getYearOfStudy());
        student.setHostelName(request.getHostelName());
        student.setRoomNumber(request.getRoomNumber());
        
        // Handle optional fields
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            student.setAddress(request.getAddress());
        }
        if (request.getGuardianName() != null && !request.getGuardianName().trim().isEmpty()) {
            student.setGuardianName(request.getGuardianName());
        }
        if (request.getGuardianMobile() != null && !request.getGuardianMobile().trim().isEmpty()) {
            student.setGuardianMobile(request.getGuardianMobile());
        }
        if (request.getGuardianRelation() != null && !request.getGuardianRelation().trim().isEmpty()) {
            student.setGuardianRelation(request.getGuardianRelation());
        }
        
        Student savedStudent = studentRepository.save(student);
        System.out.println("✅ Student registered: " + savedStudent.getUsername() + " with encrypted password");
    }
    
    private void createWarden(RegisterRequest request) {
        Warden warden = new Warden();
        setCommonUserFields(warden, request);
        warden.setEmployeeId(request.getEmployeeId());
        warden.setDepartment(request.getDepartment());
        warden.setDesignation(request.getDesignation());
        warden.setHostelAssigned(request.getHostelAssigned());
        warden.setYearsOfExperience(request.getYearsOfExperience());
        warden.setOfficeLocation(request.getOfficeLocation());
        warden.setOfficeHours(request.getOfficeHours());
        
        Warden savedWarden = wardenRepository.save(warden);
        System.out.println("✅ Warden registered: " + savedWarden.getUsername() + " with encrypted password");
    }
    
    private void createSecurity(RegisterRequest request) {
        Security security = new Security();
        setCommonUserFields(security, request);
        security.setSecurityId(request.getSecurityId());
        security.setShift(request.getShift());
        security.setGateAssigned(request.getGateAssigned());
        security.setSupervisorName(request.getSupervisorName());
        security.setSupervisorContact(request.getSupervisorContact());
        security.setYearsOfService(request.getYearsOfService());
        security.setSecurityClearanceLevel(request.getSecurityClearanceLevel());
        
        Security savedSecurity = securityRepository.save(security);
        System.out.println("✅ Security registered: " + savedSecurity.getUsername() + " with encrypted password");
    }
    
    private void createAdmin(RegisterRequest request) {
        Admin admin = new Admin();
        setCommonUserFields(admin, request);
        
        // Generate unique admin ID
        String adminId = generateUniqueAdminId();
        admin.setAdminId(adminId);
        
        // Set department and designation
        admin.setDepartment(request.getDepartment() != null ? request.getDepartment() : "Administration");
        admin.setDesignation(request.getDesignation() != null ? request.getDesignation() : "System Administrator");
        admin.setPermissionLevel("STANDARD"); // Default permission level
        
        Admin savedAdmin = adminRepository.save(admin);
        System.out.println("✅ Admin registered: " + savedAdmin.getUsername() + " with encrypted password");
        System.out.println("🔑 Generated Admin ID: " + adminId);
    }
    
    private String generateUniqueAdminId() {
        // Generate a unique admin ID based on timestamp
        return "ADMIN_" + System.currentTimeMillis();
    }
    
    private void setCommonUserFields(User user, RegisterRequest request) {
        user.setUsername(request.getUsername());
        
        // ENSURE PASSWORD IS ENCRYPTED
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(true);
        
        System.out.println("🔑 Password encrypted for: " + request.getUsername());
        System.out.println("🔑 Plain: " + request.getPassword() + " -> Encrypted: " + encryptedPassword);
    }
    
    private String generateJwtToken(User user) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            String token = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("userId", user.getId())
                    .claim("role", user.getRole().name())
                    .claim("fullName", user.getFullName())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            
            System.out.println("✅ JWT Token generated for user: " + user.getUsername());
            System.out.println("🔑 User role in token: " + user.getRole().name());
            
            return token;
        } catch (Exception e) {
            System.err.println("❌ JWT Generation Error: " + e.getMessage());
            throw new RuntimeException("JWT token generation failed", e);
        }
    }
    
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token");
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }
    
    public UserRole getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return UserRole.valueOf(claims.get("role").toString());
    }
    
    public User getAuthenticatedUser(String token) {
        String username = getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    /**
     * Get Admin entity by token (for admin-specific operations)
     */
    public Admin getAuthenticatedAdmin(String token) {
        String username = getUsernameFromToken(token);
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "username", username));
    }
    
    /**
     * Get Student entity by token (for student-specific operations)
     */
    public Student getAuthenticatedStudent(String token) {
        String username = getUsernameFromToken(token);
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));
    }
    
    /**
     * Get Warden entity by token (for warden-specific operations)
     */
    public Warden getAuthenticatedWarden(String token) {
        String username = getUsernameFromToken(token);
        return wardenRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Warden", "username", username));
    }
    
    /**
     * Get Security entity by token (for security-specific operations)
     */
    public Security getAuthenticatedSecurity(String token) {
        String username = getUsernameFromToken(token);
        return securityRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Security", "username", username));
    }
    
    public void logout(String token) {
        validateToken(token);
        // In a real application, you might want to blacklist the token
    }
    
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        System.out.println("✅ Password changed successfully for user: " + user.getUsername());
    }
    
    /**
     * Reset password without old password verification (for admin or forgot password)
     */
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        System.out.println("✅ Password reset successfully for user: " + username);
    }
    
    /**
     * Verify user identity for password reset
     */
    public boolean verifyUserIdentity(String username, String mobileNumber) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        return user.getMobileNumber().equals(mobileNumber);
    }
    
    /**
     * Get user by username or email for password reset
     */
    public User getUserByIdentifier(String identifier) {
        Optional<User> userOptional = userRepository.findByUsername(identifier);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        
        userOptional = userRepository.findByEmail(identifier);
        return userOptional.orElseThrow(() -> 
            new ResourceNotFoundException("User", "username/email", identifier));
    }
    
    /**
     * Check if user exists by username or email
     */
    public boolean userExists(String identifier) {
        return userRepository.existsByUsername(identifier) || 
               userRepository.existsByEmail(identifier);
    }
    
    /**
     * Deactivate user account (admin function)
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        System.out.println("✅ User deactivated: " + user.getUsername());
    }
    
    /**
     * Activate user account (admin function)
     */
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        System.out.println("✅ User activated: " + user.getUsername());
    }
    
    /**
     * Get all users (admin function)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get users by role (admin function)
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Search users by name (admin function)
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    
    /**
     * Get admin-specific information
     */
    public String getAdminId(Long userId) {
        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", userId));
        return admin.getAdminId();
    }
    
    /**
     * Get admin permission level
     */
    public String getAdminPermissionLevel(Long userId) {
        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", userId));
        return admin.getPermissionLevel();
    }
    
    /**
     * Check if user is admin
     */
    public boolean isUserAdmin(Long userId) {
        Optional<Admin> admin = adminRepository.findById(userId);
        return admin.isPresent();
    }
    
    /**
     * Get user type specific information
     */
    public String getUserSpecificId(Long userId, UserRole role) {
        switch (role) {
            case STUDENT:
                Student student = studentRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student", "id", userId));
                return student.getRollNumber();
            case WARDEN:
                Warden warden = wardenRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Warden", "id", userId));
                return warden.getEmployeeId();
            case SECURITY:
                Security security = securityRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Security", "id", userId));
                return security.getSecurityId();
            case ADMIN:
                Admin admin = adminRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", userId));
                return admin.getAdminId();
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
    }
    
    /**
     * Get user count by role (for admin dashboard)
     */
    public long getUserCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * Get recently logged in users (admin function)
     */
    public List<User> getRecentlyLoggedInUsers(int hours) {
        LocalDateTime sinceDate = LocalDateTime.now().minusHours(hours);
        return userRepository.findRecentlyLoggedInUsers(sinceDate);
    }
}