package com.mit.outpass.dto;

import com.mit.outpass.enums.UserRole;

public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private UserRole role;
    private Long userId;
    private String message;
    
    public LoginResponse() {}
    
    public LoginResponse(String token, String username, String fullName, UserRole role, Long userId) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.userId = userId;
        this.message = "Login successful";
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}