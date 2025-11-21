package com.mit.outpass.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Roll number/ID is required")
    private String loginId;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String role;
    
    public LoginRequest() {}
    
    public LoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
    
    // Getters and Setters
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
