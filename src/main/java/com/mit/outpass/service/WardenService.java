package com.mit.outpass.service;

import com.mit.outpass.dto.OutpassResponse;
import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Student;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.enums.OutpassStatus;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.OutpassRepository;
import com.mit.outpass.repository.StudentRepository;
import com.mit.outpass.repository.WardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WardenService {
    
    @Autowired
    private WardenRepository wardenRepository;
    
    @Autowired
    private OutpassRepository outpassRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private OutpassService outpassService;
    
    public Warden getWardenById(Long id) {
        return wardenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warden", "id", id));
    }
    
    public Warden getWardenByEmployeeId(String employeeId) {
        return wardenRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Warden", "employeeId", employeeId));
    }
    
    public Warden getWardenByUsername(String username) {
        return wardenRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Warden", "username", username));
    }
    
    public List<Warden> getAllWardens() {
        return wardenRepository.findAll();
    }
    
    public List<Warden> getWardensByDepartment(String department) {
        return wardenRepository.findByDepartment(department);
    }
    
    public List<Warden> getWardensByHostel(String hostelAssigned) {
        return wardenRepository.findByHostelAssigned(hostelAssigned);
    }
    
    public Warden updateWarden(Warden warden) {
        return wardenRepository.save(warden);
    }
    
    // Hostel-specific outpass methods
    public List<OutpassResponse> getPendingOutpassesByHostel(String hostelName) {
        System.out.println("🏠 Fetching pending outpasses for hostel: " + hostelName);
        List<Outpass> outpasses = outpassRepository.findPendingOutpassesByHostel(hostelName);
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OutpassResponse> getApprovedOutpassesByHostel(String hostelName) {
        System.out.println("🏠 Fetching approved outpasses for hostel: " + hostelName);
        List<Outpass> outpasses = outpassRepository.findApprovedOutpassesByHostel(hostelName);
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OutpassResponse> getActiveOutpassesByHostel(String hostelName) {
        System.out.println("🏠 Fetching active outpasses for hostel: " + hostelName);
        List<Outpass> outpasses = outpassRepository.findActiveOutpassesByHostel(hostelName);
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OutpassResponse> getAllOutpassesByHostel(String hostelName) {
        System.out.println("🏠 Fetching all outpasses for hostel: " + hostelName);
        List<Outpass> outpasses = outpassRepository.findAllOutpassesByHostel(hostelName);
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OutpassResponse> getOutpassesByHostelAndStatus(String hostelName, String status) {
        System.out.println("🏠 Fetching outpasses for hostel: " + hostelName + " with status: " + status);
        
        switch (status.toUpperCase()) {
            case "PENDING":
                return getPendingOutpassesByHostel(hostelName);
            case "APPROVED":
                return getApprovedOutpassesByHostel(hostelName);
            case "ACTIVE":
                return getActiveOutpassesByHostel(hostelName);
            case "ALL":
            default:
                return getAllOutpassesByHostel(hostelName);
        }
    }
    
    // Statistics methods with hostel filtering
    public Map<String, Long> getWardenStats(String hostelName) {
        System.out.println("📊 Fetching statistics for hostel: " + hostelName);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", outpassRepository.countByStatusAndHostel(OutpassStatus.PENDING, hostelName));
        stats.put("approved", outpassRepository.countByStatusAndHostel(OutpassStatus.APPROVED, hostelName));
        stats.put("active", outpassRepository.countByStatusAndHostel(OutpassStatus.ACTIVE, hostelName));
        stats.put("rejected", outpassRepository.countByStatusAndHostel(OutpassStatus.REJECTED, hostelName));
        stats.put("completed", outpassRepository.countByStatusAndHostel(OutpassStatus.COMPLETED, hostelName));
        stats.put("cancelled", outpassRepository.countByStatusAndHostel(OutpassStatus.CANCELLED, hostelName));
        stats.put("total", outpassRepository.countAllOutpassesByHostel(hostelName));
        
        System.out.println("📊 Stats for " + hostelName + ": " + stats);
        return stats;
    }
    
    public Map<String, Object> getDetailedStatistics(String hostelName) {
        System.out.println("📈 Fetching detailed statistics for hostel: " + hostelName);
        
        Map<String, Object> stats = new HashMap<>();
        List<OutpassResponse> allOutpasses = getAllOutpassesByHostel(hostelName);
        
        // Basic counts
        stats.put("totalApplications", allOutpasses.size());
        stats.put("pendingApplications", allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.PENDING).count());
        stats.put("approvedApplications", allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.APPROVED).count());
        stats.put("rejectedApplications", allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.REJECTED).count());
        stats.put("activeOutpasses", allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.ACTIVE).count());
        stats.put("completedOutpasses", allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.COMPLETED).count());
        stats.put("lateReturns", allOutpasses.stream().filter(o -> Boolean.TRUE.equals(o.getIsLateReturn())).count());
        
        // Calculate rates
        if (allOutpasses.size() > 0) {
            double approvalRate = ((double) allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.APPROVED).count() / allOutpasses.size()) * 100;
            double rejectionRate = ((double) allOutpasses.stream().filter(o -> o.getStatus() == OutpassStatus.REJECTED).count() / allOutpasses.size()) * 100;
            stats.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
            stats.put("rejectionRate", Math.round(rejectionRate * 100.0) / 100.0);
        } else {
            stats.put("approvalRate", 0.0);
            stats.put("rejectionRate", 0.0);
        }
        
        // Recent outpasses (last 10)
        List<OutpassResponse> recentOutpasses = allOutpasses.stream()
                .limit(10)
                .collect(Collectors.toList());
        stats.put("recentOutpasses", recentOutpasses);
        
        // Pending for review
        List<OutpassResponse> pendingReview = getPendingOutpassesByHostel(hostelName);
        stats.put("pendingReview", pendingReview);
        
        System.out.println("📈 Detailed stats for " + hostelName + ": " + stats);
        return stats;
    }
    
    // Security validation methods
    public boolean canWardenReviewOutpass(Long wardenId, Long outpassId) {
        try {
            Warden warden = getWardenById(wardenId);
            Outpass outpass = outpassRepository.findById(outpassId)
                    .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
            
            String wardenHostel = warden.getHostelAssigned();
            String studentHostel = outpass.getStudent().getHostelName();
            
            boolean canReview = wardenHostel != null && 
                              wardenHostel.equals(studentHostel) && 
                              outpass.getStatus() == OutpassStatus.PENDING;
            
            System.out.println("🔐 Warden " + wardenId + " (" + wardenHostel + ") " +
                             "can review outpass " + outpassId + " (" + studentHostel + "): " + canReview);
            
            return canReview;
        } catch (Exception e) {
            System.out.println("❌ Error checking warden permissions: " + e.getMessage());
            return false;
        }
    }
    
    public String getWardenHostel(Long wardenId) {
        Warden warden = getWardenById(wardenId);
        return warden.getHostelAssigned();
    }
    
    public String getWardenHostelByUsername(String username) {
        Warden warden = getWardenByUsername(username);
        return warden.getHostelAssigned();
    }
    // In your WardenService.java - Update the convertToResponse method
// In your WardenService.java - COMPLETE FIXED VERSION
private OutpassResponse convertToResponse(Outpass outpass) {
    OutpassResponse response = new OutpassResponse();
    response.setId(outpass.getId());
    response.setStudentName(outpass.getStudent().getFullName());
    response.setStudentRollNumber(outpass.getStudent().getRollNumber());
    
    // CRITICAL: Ensure hostel name is set
    response.setHostelName(outpass.getStudent().getHostelName());
    
    // CRITICAL: Ensure reason is properly set with fallback
    String reason = outpass.getReason();
    if (reason == null || reason.trim().isEmpty()) {
        reason = "No reason provided";
        System.out.println("⚠️ Outpass ID " + outpass.getId() + " has empty reason, using fallback");
    }
    response.setReason(reason);
    
    response.setLeaveStartDate(outpass.getLeaveStartDate());
    response.setExpectedReturnDate(outpass.getExpectedReturnDate());
    response.setDestination(outpass.getDestination());
    response.setStatus(outpass.getStatus());
    response.setWardenComments(outpass.getWardenComments());
    response.setSecurityComments(outpass.getSecurityComments());
    response.setCreatedAt(outpass.getCreatedAt());
    response.setActualDepartureTime(outpass.getActualDepartureTime());
    response.setActualReturnTime(outpass.getActualReturnTime());
    response.setIsLateReturn(outpass.getIsLateReturn());
    response.setEmergencyContactName(outpass.getEmergencyContactName());
    response.setEmergencyContactNumber(outpass.getEmergencyContactNumber());
    response.setEmergencyContactRelation(outpass.getEmergencyContactRelation());
    response.setLateReturnReason(outpass.getLateReturnReason());
    
    // Set reviewed by name if available
    if (outpass.getReviewedBy() != null) {
        response.setReviewedByName(outpass.getReviewedBy().getFullName());
    }
    
    // Debug logging
    System.out.println("✅ Converted outpass ID " + outpass.getId() + 
                      " - Reason: " + response.getReason() +
                      ", Hostel: " + response.getHostelName());
    
    return response;
}
}