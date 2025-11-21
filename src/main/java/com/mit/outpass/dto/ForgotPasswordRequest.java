package com.mit.outpass.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Institutional ID is required")
    private String loginId;
    
    @NotBlank(message = "Mobile number is required for verification")
    private String mobileNumber;
    
    public ForgotPasswordRequest() {}
    
    // Getters and Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}
