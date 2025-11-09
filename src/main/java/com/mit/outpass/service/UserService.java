package com.mit.outpass.service;

import com.mit.outpass.controller.UserController.UserProfileUpdateRequest;
import com.mit.outpass.dto.UserDTO;
import com.mit.outpass.entity.User;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get user profile by ID
     */
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return convertToDTO(user);
    }
    
    /**
     * Update user profile - allows users to update their own basic information
     */
    @Transactional
    public UserDTO updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Update basic profile fields
        if (updateRequest.getFullName() != null && !updateRequest.getFullName().trim().isEmpty()) {
            user.setFullName(updateRequest.getFullName().trim());
        }
        
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmailAndIdNot(updateRequest.getEmail().trim(), userId)) {
                throw new IllegalArgumentException("Email is already taken by another user");
            }
            user.setEmail(updateRequest.getEmail().trim());
        }
        
        if (updateRequest.getMobileNumber() != null && !updateRequest.getMobileNumber().trim().isEmpty()) {
            user.setMobileNumber(updateRequest.getMobileNumber().trim());
        }
        
        // Set updated timestamp
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setRole(user.getRole());
        dto.setActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin()); // This will work now
                if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().toString());
        }
        
        return dto;
    }
}