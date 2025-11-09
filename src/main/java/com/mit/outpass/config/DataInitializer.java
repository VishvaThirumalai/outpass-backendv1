package com.mit.outpass.config;

import com.mit.outpass.entity.Admin;
import com.mit.outpass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUser("admin", "admin123@gmail.com", "System Administrator", 
                       "7092980042", "ADMIN001", "Administration", 
                       "System Administrator", "SUPER_ADMIN", "admin123");
        
        createAdminUser("thiru", "thiru@gmail.com", "Thirumalai", 
                       "9843710041", "ADMIN_1762151131040", "Security Management", 
                       "Administrative Officer", "ADMIN", "thiru123");
    }

    private void createAdminUser(String username, String email, String fullName, 
                               String mobileNumber, String adminId, String department,
                               String designation, String permissionLevel, String password) {
        
        // Check if user already exists
        if (userRepository.findByUsername(username).isEmpty()) {
            // Create Admin entity - this will insert into BOTH tables automatically
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setFullName(fullName);
            admin.setEmail(email);
            admin.setMobileNumber(mobileNumber);
            admin.setIsActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            
            // Set admin-specific fields
            admin.setAdminId(adminId);
            admin.setDepartment(department);
            admin.setDesignation(designation);
            admin.setPermissionLevel(permissionLevel);
            
            // Save through UserRepository - this handles both tables due to inheritance
            userRepository.save(admin);
            
            System.out.println("✅ Admin user created in both tables: " + username + "/" + password);
        } else {
            System.out.println("ℹ️ User already exists in both tables: " + username);
        }
    }
}