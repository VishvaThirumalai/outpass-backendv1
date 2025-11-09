package com.mit.outpass.service;

import com.mit.outpass.dto.OutpassRequest;
import com.mit.outpass.dto.OutpassResponse;
import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Security;
import com.mit.outpass.entity.Student;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.enums.OutpassStatus;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.OutpassRepository;
import com.mit.outpass.repository.SecurityRepository;
import com.mit.outpass.repository.StudentRepository;
import com.mit.outpass.repository.WardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for outpass operations
 */
@Service
public class OutpassService {
    
    @Autowired
    private OutpassRepository outpassRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private WardenRepository wardenRepository;
    
    @Autowired
    private SecurityRepository securityRepository;
    
    /**
     * Apply for new outpass
     */
   /**
 * Apply for new outpass
 */
public OutpassResponse applyOutpass(Long studentId, OutpassRequest request) {
    System.out.println("📝 Applying outpass for student ID: " + studentId);
    System.out.println("📅 Leave Start Date: " + request.getLeaveStartDate());
    System.out.println("📅 Expected Return Date: " + request.getExpectedReturnDate());
    System.out.println("📅 Current Time: " + LocalDateTime.now());
    
    // Enhanced date validation with better error messages
    if (request.getLeaveStartDate() == null) {
        throw new IllegalArgumentException("Leave start date is required");
    }
    
    if (request.getExpectedReturnDate() == null) {
        throw new IllegalArgumentException("Expected return date is required");
    }
    
    // Allow leave start date to be as early as current time (for immediate departure)
    if (request.getLeaveStartDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
        throw new IllegalArgumentException("Leave start date must be current or future time");
    }
    
    if (!request.getExpectedReturnDate().isAfter(request.getLeaveStartDate())) {
        throw new IllegalArgumentException("Return date must be after leave start date");
    }
    
    // Validate minimum duration (at least 30 minutes)
    long minutesDifference = java.time.Duration.between(request.getLeaveStartDate(), request.getExpectedReturnDate()).toMinutes();
    if (minutesDifference < 30) {
        throw new IllegalArgumentException("Minimum outpass duration is 30 minutes");
    }

    Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

    Outpass outpass = new Outpass();
    outpass.setStudent(student);
    outpass.setReason(request.getReason());
    outpass.setLeaveStartDate(request.getLeaveStartDate());
    outpass.setExpectedReturnDate(request.getExpectedReturnDate());
    outpass.setDestination(request.getDestination());
    outpass.setEmergencyContactName(request.getEmergencyContactName());
    outpass.setEmergencyContactNumber(request.getEmergencyContactNumber());
    outpass.setEmergencyContactRelation(request.getEmergencyContactRelation());
    outpass.setStatus(OutpassStatus.PENDING);
    outpass.setCreatedAt(LocalDateTime.now());

    Outpass savedOutpass = outpassRepository.save(outpass);
    System.out.println("✅ Outpass created successfully with ID: " + savedOutpass.getId());

    return convertToResponse(savedOutpass);
}
    /**
     * Edit existing outpass (only if pending)
     */
    public OutpassResponse editOutpass(Long outpassId, Long studentId, OutpassRequest request) {
        Outpass outpass = outpassRepository.findById(outpassId)
                .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
        
        // Verify ownership
        if (!outpass.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("You can only edit your own outpass");
        }
        
        // Check if can be edited
        if (!outpass.canBeEdited()) {
            throw new IllegalStateException("Outpass cannot be edited in current status: " + outpass.getStatus());
        }
        
        // Validate dates
        if (request.getExpectedReturnDate().isBefore(request.getLeaveStartDate())) {
            throw new IllegalArgumentException("Return date cannot be before start date");
        }
        
        // Update outpass
        outpass.setReason(request.getReason());
        outpass.setLeaveStartDate(request.getLeaveStartDate());
        outpass.setExpectedReturnDate(request.getExpectedReturnDate());
        outpass.setDestination(request.getDestination());
        outpass.setEmergencyContactName(request.getEmergencyContactName());
        outpass.setEmergencyContactNumber(request.getEmergencyContactNumber());
        outpass.setEmergencyContactRelation(request.getEmergencyContactRelation());
        
        outpass = outpassRepository.save(outpass);
        return convertToResponse(outpass);
    }
    
    /**
     * Cancel outpass (only if pending or approved)
     */
    public void cancelOutpass(Long outpassId, Long studentId) {
        Outpass outpass = outpassRepository.findById(outpassId)
                .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
        
        // Verify ownership
        if (!outpass.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("You can only cancel your own outpass");
        }
        
        // Check if can be cancelled
        if (!outpass.canBeCancelled()) {
            throw new IllegalStateException("Outpass cannot be cancelled in current status: " + outpass.getStatus());
        }
        
        outpass.setStatus(OutpassStatus.CANCELLED);
        outpassRepository.save(outpass);
    }
    
    /**
     * Get student's outpasses
     */
    public List<OutpassResponse> getStudentOutpasses(Long studentId) {
        List<Outpass> outpasses = outpassRepository.findRecentOutpassesByStudentId(studentId);
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all pending outpasses (for warden)
     */
    public List<OutpassResponse> getPendingOutpasses() {
        List<Outpass> outpasses = outpassRepository.findPendingOutpasses();
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Review outpass (approve/reject by warden)
     */
    public OutpassResponse reviewOutpass(Long outpassId, Long wardenId, boolean approved, String comments) {
        Outpass outpass = outpassRepository.findById(outpassId)
                .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
        
        Warden warden = wardenRepository.findById(wardenId)
                .orElseThrow(() -> new ResourceNotFoundException("Warden", "id", wardenId));
        
        // Check if outpass is in pending status
        if (outpass.getStatus() != OutpassStatus.PENDING) {
            throw new IllegalStateException("Only pending outpasses can be reviewed");
        }
        
        // Update outpass
        outpass.setStatus(approved ? OutpassStatus.APPROVED : OutpassStatus.REJECTED);
        outpass.setReviewedBy(warden);
        outpass.setWardenComments(comments);
        outpass.setReviewedAt(LocalDateTime.now());
        
        outpass = outpassRepository.save(outpass);
        return convertToResponse(outpass);
    }
    
    /**
     * Get approved outpasses (for security)
     */
    public List<OutpassResponse> getApprovedOutpasses() {
        List<Outpass> outpasses = outpassRepository.findApprovedOutpasses();
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark student departure
     */
    public OutpassResponse markDeparture(Long outpassId, Long securityId, String comments) {
        Outpass outpass = outpassRepository.findById(outpassId)
                .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
        
        Security security = securityRepository.findById(securityId)
                .orElseThrow(() -> new ResourceNotFoundException("Security", "id", securityId));
        
        // Check if outpass is approved
        if (outpass.getStatus() != OutpassStatus.APPROVED) {
            throw new IllegalStateException("Only approved outpasses can be marked for departure");
        }
        
        // Update outpass
        outpass.setStatus(OutpassStatus.ACTIVE);
        outpass.setDepartureMarkedBy(security);
        outpass.setActualDepartureTime(LocalDateTime.now());
        outpass.setSecurityComments(comments);
        
        outpass = outpassRepository.save(outpass);
        return convertToResponse(outpass);
    }
    
    /**
     * Mark student return
     */
public OutpassResponse markReturn(Long outpassId, Long securityId, String comments, String lateReturnReason) {
    System.out.println("🔄 Marking return for outpass ID: " + outpassId);
    
    Outpass outpass = outpassRepository.findById(outpassId)
            .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", outpassId));
    
    Security security = securityRepository.findById(securityId)
            .orElseThrow(() -> new ResourceNotFoundException("Security", "id", securityId));
    
    // Check if outpass is active
    if (outpass.getStatus() != OutpassStatus.ACTIVE) {
        throw new IllegalStateException("Only active outpasses can be marked for return. Current status: " + outpass.getStatus());
    }
    
    LocalDateTime returnTime = LocalDateTime.now();
    LocalDateTime departureTime = outpass.getActualDepartureTime();
    
    // Check if departure window has expired (24 hours after departure)
    boolean isExpired = false;
    if (departureTime != null) {
        LocalDateTime departureWindowEnd = departureTime.plusHours(24);
        isExpired = returnTime.isAfter(departureWindowEnd);
        // REMOVED THE EXCEPTION - JUST SETS THE FLAG
    }
    
    // Check if return is late compared to expected return time
    boolean isLateReturn = returnTime.isAfter(outpass.getExpectedReturnDate());
    
    // Update outpass - ALLOW COMPLETION EVEN IF EXPIRED
    outpass.setStatus(OutpassStatus.COMPLETED);
    outpass.setReturnMarkedBy(security);
    outpass.setActualReturnTime(returnTime);
    outpass.setIsLateReturn(isLateReturn || isExpired); // Mark as late if expired
    
    // Build security comments
    StringBuilder finalComments = new StringBuilder();
    String existingComments = outpass.getSecurityComments();
    
    if (existingComments != null && !existingComments.trim().isEmpty()) {
        finalComments.append(existingComments).append(" | ");
    }
    
    // Handle expired outpasses
    if (isExpired) {
        finalComments.append("EXPIRED RETURN: ").append(comments);
        
        // Build comprehensive late return reason for expired outpass
        StringBuilder expiredReason = new StringBuilder();
        expiredReason.append("Departure window expired (24h limit exceeded). ");
        
        if (isLateReturn) {
            long lateHours = java.time.Duration.between(outpass.getExpectedReturnDate(), returnTime).toHours();
            expiredReason.append("Also returned ").append(lateHours).append(" hours after expected return time. ");
        }
        
        if (lateReturnReason != null && !lateReturnReason.trim().isEmpty()) {
            expiredReason.append("Reason: ").append(lateReturnReason.trim());
        } else {
            expiredReason.append("No specific reason provided.");
        }
        
        outpass.setLateReturnReason(expiredReason.toString());
        System.out.println("⚠️ Completed EXPIRED outpass ID: " + outpassId);
        
    } else {
        // Normal return processing
        finalComments.append("Return: ").append(comments);
        
        if (isLateReturn) {
            finalComments.append(" (LATE RETURN)");
            
            // Store late return reason if provided
            if (lateReturnReason != null && !lateReturnReason.trim().isEmpty()) {
                outpass.setLateReturnReason(lateReturnReason.trim());
            } else {
                // Auto-generate reason if none provided for late return
                long lateMinutes = java.time.Duration.between(outpass.getExpectedReturnDate(), returnTime).toMinutes();
                outpass.setLateReturnReason("Returned " + lateMinutes + " minutes late. No reason provided.");
            }
        }
    }
    
    outpass.setSecurityComments(finalComments.toString());
    
    // Log the action
    System.out.println("✅ Return marked successfully - Outpass ID: " + outpassId + 
                      ", Expired: " + isExpired +
                      ", Late: " + isLateReturn + 
                      ", Security ID: " + securityId);
    
    outpass = outpassRepository.save(outpass);
    return convertToResponse(outpass);
}
    
    /**
     * Get active outpasses (students currently out)
     */
    public List<OutpassResponse> getActiveOutpasses() {
        List<Outpass> outpasses = outpassRepository.findActiveOutpasses();
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all outpasses (for warden/admin)
     */
    public List<OutpassResponse> getAllOutpasses() {
        List<Outpass> outpasses = outpassRepository.findAll();
        return outpasses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get outpass by ID
     */
    public OutpassResponse getOutpassById(Long id) {
        Outpass outpass = outpassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outpass", "id", id));
        return convertToResponse(outpass);
    }
    // In your OutpassService.java - UPDATE THE convertToResponse METHOD
/**
 * Convert Outpass entity to OutpassResponse DTO - FIXED VERSION
 */
/**
 * Convert Outpass entity to OutpassResponse DTO - COMPLETE VERSION
 */
private OutpassResponse convertToResponse(Outpass outpass) {
    OutpassResponse response = new OutpassResponse();
    response.setId(outpass.getId());
    response.setStudentName(outpass.getStudent().getFullName());
    response.setStudentRollNumber(outpass.getStudent().getRollNumber());
    response.setHostelName(outpass.getStudent().getHostelName());
    response.setReason(outpass.getReason());
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
    response.setLateReturnReason(outpass.getLateReturnReason()); // ADD THIS LINE
    
    // Set reviewed by name if available
    if (outpass.getReviewedBy() != null) {
        response.setReviewedByName(outpass.getReviewedBy().getFullName());
    }
    
    // Log late return information for debugging
    if (outpass.getIsLateReturn() != null && outpass.getIsLateReturn()) {
        System.out.println("⚠️ Converted outpass ID " + outpass.getId() + 
                          " - LATE RETURN: " + outpass.getIsLateReturn() +
                          ", Reason: " + (outpass.getLateReturnReason() != null ? outpass.getLateReturnReason() : "No reason"));
    } else {
        System.out.println("✅ Converted outpass ID " + outpass.getId() + 
                          " with hostel: " + response.getHostelName() +
                          ", Late Return: " + response.getIsLateReturn());
    }
    
    return response;
}
}