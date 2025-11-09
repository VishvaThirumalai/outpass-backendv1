package com.mit.outpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for MIT Hostel Outpass Management System
 * 
 * @author MIT Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class OutpassManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutpassManagementApplication.class, args);
        
        System.out.println("===========================================");
        System.out.println("MIT Hostel Outpass Management System Started");
        System.out.println("Backend API: http://localhost:8080/api");
        System.out.println("Database: MySQL");
        System.out.println("===========================================");
    }
}