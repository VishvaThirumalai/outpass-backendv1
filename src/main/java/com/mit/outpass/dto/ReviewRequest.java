// ReviewRequest.java
package com.mit.outpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {
    @NotNull(message = "Approval status is required")
    private Boolean approved;
    
    @NotBlank(message = "Comments are required")
    private String comments;
    
    public ReviewRequest() {}
    
    public ReviewRequest(Boolean approved, String comments) {
        this.approved = approved;
        this.comments = comments;
    }
    
    // Getters and Setters
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}