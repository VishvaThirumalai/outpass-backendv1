package com.mit.outpass.controller;

import com.mit.outpass.dto.ApiResponse;
import com.mit.outpass.dto.OutpassResponse;
import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Security;
import com.mit.outpass.entity.Student;
import com.mit.outpass.entity.Warden;
import com.mit.outpass.enums.OutpassStatus;
import com.mit.outpass.repository.OutpassRepository;
import com.mit.outpass.repository.StudentRepository;
import com.mit.outpass.repository.WardenRepository;
import com.mit.outpass.service.AuthService;
import com.mit.outpass.service.OutpassService;
import com.mit.outpass.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for security operations
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SecurityController {
    
    @Autowired
    private OutpassService outpassService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private OutpassRepository outpassRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private WardenRepository wardenRepository;
    
    /**
     * Get security officer profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SecurityProfileResponse>> getSecurityProfile(
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long securityId = authService.getUserIdFromToken(token);
            
            // Get security officer details
            Security security = securityService.getSecurityById(securityId);
            
            // Create profile response
            SecurityProfileResponse profileResponse = new SecurityProfileResponse();
            profileResponse.setId(security.getId());
            profileResponse.setName(security.getFullName()); // Use getFullName() from User class
            profileResponse.setUsername(security.getUsername());
            profileResponse.setSecurityId(security.getSecurityId());
            profileResponse.setEmail(security.getEmail());
            profileResponse.setPhone(security.getMobileNumber()); // Use getMobileNumber() from User class
            profileResponse.setShift(security.getShift());
            profileResponse.setGateAssigned(security.getGateAssigned());
            profileResponse.setCreatedAt(security.getCreatedAt());
            
            ApiResponse<SecurityProfileResponse> response = ApiResponse.success("Security profile retrieved successfully", profileResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<SecurityProfileResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all approved outpasses (ready for departure)
     */
    @GetMapping("/outpasses/approved")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getApprovedOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            List<OutpassResponse> outpasses = outpassService.getApprovedOutpasses();
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Approved outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get all active outpasses (students currently out)
     */
    @GetMapping("/outpasses/active")
    public ResponseEntity<ApiResponse<List<OutpassResponse>>> getActiveOutpasses(
            @RequestHeader("Authorization") String token) {
        try {
            List<OutpassResponse> outpasses = outpassService.getActiveOutpasses();
            ApiResponse<List<OutpassResponse>> response = ApiResponse.success("Active outpasses retrieved successfully", outpasses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<OutpassResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Mark student departure
     */
    @PutMapping("/outpass/{id}/departure")
    public ResponseEntity<ApiResponse<OutpassResponse>> markDeparture(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody DepartureRequest request) {
        try {
            // Remove "Bearer " prefix
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long securityId = authService.getUserIdFromToken(token);
            String comments = request.getComments() != null ? request.getComments() : "Student departed";
            
            OutpassResponse outpass = outpassService.markDeparture(id, securityId, comments);
            
            ApiResponse<OutpassResponse> response = ApiResponse.success("Student departure marked successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Mark student return
     */
    /**
 * Mark student return
 */
@PutMapping("/outpass/{id}/return")
public ResponseEntity<ApiResponse<OutpassResponse>> markReturn(
        @RequestHeader("Authorization") String token,
        @PathVariable Long id,
        @RequestBody ReturnRequest request) {
    try {
        // Remove "Bearer " prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long securityId = authService.getUserIdFromToken(token);
        String comments = request.getComments() != null ? request.getComments() : "Student returned";
        String lateReturnReason = request.getLateReturnReason(); // Get the late return reason
        
        // Pass all 4 parameters to service method
        OutpassResponse outpass = outpassService.markReturn(id, securityId, comments, lateReturnReason);
        
        ApiResponse<OutpassResponse> response = ApiResponse.success("Student return marked successfully", outpass);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
        ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
    
    /**
     * Get specific outpass details
     */
    @GetMapping("/outpass/{id}")
    public ResponseEntity<ApiResponse<OutpassResponse>> getOutpass(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            OutpassResponse outpass = outpassService.getOutpassById(id);
            ApiResponse<OutpassResponse> response = ApiResponse.success("Outpass details retrieved successfully", outpass);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<OutpassResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get dashboard data for security
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SecurityDashboardData>> getDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            List<OutpassResponse> approvedOutpasses = outpassService.getApprovedOutpasses();
            List<OutpassResponse> activeOutpasses = outpassService.getActiveOutpasses();
            List<OutpassResponse> allOutpasses = outpassService.getAllOutpasses();
            
            SecurityDashboardData dashboardData = new SecurityDashboardData();
            dashboardData.setApprovedOutpasses(approvedOutpasses.size());
            dashboardData.setActiveOutpasses(activeOutpasses.size());
            
            // Count completed today
            int completedToday = 0;
            LocalDateTime today = LocalDateTime.now();
            for (OutpassResponse outpass : allOutpasses) {
                if (outpass.getStatus() == OutpassStatus.COMPLETED && 
                    outpass.getActualReturnTime() != null &&
                    outpass.getActualReturnTime().toLocalDate().equals(today.toLocalDate())) {
                    completedToday++;
                }
            }
            dashboardData.setCompletedToday(completedToday);
            
            // Count late returns
            int lateReturns = 0;
            for (OutpassResponse outpass : allOutpasses) {
                if (Boolean.TRUE.equals(outpass.getIsLateReturn())) {
                    lateReturns++;
                }
            }
            dashboardData.setLateReturns(lateReturns);
            
            dashboardData.setPendingDepartures(approvedOutpasses);
            dashboardData.setPendingReturns(activeOutpasses);
            
            // Get recent activity
            List<OutpassResponse> recentActivity = new ArrayList<>();
            for (OutpassResponse outpass : allOutpasses) {
                if (outpass.getActualDepartureTime() != null || outpass.getActualReturnTime() != null) {
                    recentActivity.add(outpass);
                    if (recentActivity.size() >= 10) {
                        break;
                    }
                }
            }
            dashboardData.setRecentActivity(recentActivity);
            
            ApiResponse<SecurityDashboardData> response = ApiResponse.success("Dashboard data retrieved successfully", dashboardData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<SecurityDashboardData> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get today's outpass activities
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayActivity>> getTodayActivity(
            @RequestHeader("Authorization") String token) {
        try {
            List<OutpassResponse> allOutpasses = outpassService.getAllOutpasses();
            LocalDateTime today = LocalDateTime.now();
            
            TodayActivity todayActivity = new TodayActivity();
            
            // Get departures today
            List<OutpassResponse> departuresToday = new ArrayList<>();
            for (OutpassResponse outpass : allOutpasses) {
                if (outpass.getActualDepartureTime() != null && 
                    outpass.getActualDepartureTime().toLocalDate().equals(today.toLocalDate())) {
                    departuresToday.add(outpass);
                }
            }
            todayActivity.setDeparturesToday(departuresToday);
            
            // Get returns today
            List<OutpassResponse> returnsToday = new ArrayList<>();
            for (OutpassResponse outpass : allOutpasses) {
                if (outpass.getActualReturnTime() != null && 
                    outpass.getActualReturnTime().toLocalDate().equals(today.toLocalDate())) {
                    returnsToday.add(outpass);
                }
            }
            todayActivity.setReturnsToday(returnsToday);
            
            // Get expected returns
            List<OutpassResponse> expectedReturns = new ArrayList<>();
            for (OutpassResponse outpass : allOutpasses) {
                if (outpass.getStatus() == OutpassStatus.ACTIVE && 
                    outpass.getExpectedReturnDate().toLocalDate().equals(today.toLocalDate())) {
                    expectedReturns.add(outpass);
                }
            }
            todayActivity.setExpectedReturns(expectedReturns);
            
            ApiResponse<TodayActivity> response = ApiResponse.success("Today's activity retrieved successfully", todayActivity);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<TodayActivity> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Debug endpoint to check approved outpasses
     */
    @GetMapping("/debug/approved-outpasses")
    public ResponseEntity<ApiResponse<Object>> debugApprovedOutpasses() {
        try {
            System.out.println("🔍 DEBUG: Checking approved outpasses...");
            
            // Get all outpasses from repository
            List<Outpass> allOutpasses = outpassRepository.findAll();
            System.out.println("📊 Total outpasses in database: " + allOutpasses.size());
            
            // Count status
            Map<OutpassStatus, Long> statusCount = new HashMap<>();
            for (Outpass outpass : allOutpasses) {
                OutpassStatus status = outpass.getStatus();
                statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
            }
            
            System.out.println("📈 Outpass status distribution:");
            for (Map.Entry<OutpassStatus, Long> entry : statusCount.entrySet()) {
                System.out.println("   " + entry.getKey() + ": " + entry.getValue());
            }
            
            // Get approved outpasses using repository method
            List<Outpass> approvedOutpasses = outpassRepository.findApprovedOutpasses();
            System.out.println("✅ Approved outpasses found by repository: " + approvedOutpasses.size());
            
            // Debug each approved outpass
            for (Outpass outpass : approvedOutpasses) {
                System.out.println("   - ID: " + outpass.getId() + 
                                 ", Student: " + outpass.getStudent().getFullName() +
                                 ", Leave: " + outpass.getLeaveStartDate());
            }
            
            // Create debug info
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("totalOutpasses", allOutpasses.size());
            debugInfo.put("statusDistribution", statusCount);
            debugInfo.put("approvedOutpassesCount", approvedOutpasses.size());
            
            // Convert approved outpasses to list of maps
            List<Map<String, Object>> approvedOutpassesList = new ArrayList<>();
            for (Outpass outpass : approvedOutpasses) {
                Map<String, Object> opInfo = new HashMap<>();
                opInfo.put("id", outpass.getId());
                opInfo.put("studentName", outpass.getStudent().getFullName());
                opInfo.put("leaveStartDate", outpass.getLeaveStartDate());
                opInfo.put("status", outpass.getStatus());
                approvedOutpassesList.add(opInfo);
            }
            debugInfo.put("approvedOutpasses", approvedOutpassesList);
            
            return ResponseEntity.ok(ApiResponse.success("Debug information", debugInfo));
            
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Create test approved outpass
     */
    @PostMapping("/test/create-approved-outpass")
    public ResponseEntity<ApiResponse<String>> createTestApprovedOutpass() {
        try {
            System.out.println("🎯 Creating test approved outpass...");
            
            // Get or create a test student
            Optional<Student> existingStudent = studentRepository.findByRollNumber("MIT2024001");
            Student student;
            if (existingStudent.isPresent()) {
                student = existingStudent.get();
            } else {
                student = new Student();
                student.setFullName("Test Student");
                student.setRollNumber("MIT2024001");
                student.setEmail("test.student@mit.edu");
                student.setHostelName("Dr. Kalam Hostel");
                student.setRoomNumber("101");
                student.setCourse("Computer Science");
                student.setDegree("B.Tech");
                student.setYearOfStudy(2);
                student = studentRepository.save(student);
            }
            
            // Create approved outpass
            Outpass outpass = new Outpass();
            outpass.setStudent(student);
            outpass.setReason("Medical checkup at city hospital");
            outpass.setLeaveStartDate(LocalDateTime.now().plusHours(1));
            outpass.setExpectedReturnDate(LocalDateTime.now().plusHours(4));
            outpass.setDestination("City Medical Center");
            outpass.setEmergencyContactName("Parent");
            outpass.setEmergencyContactNumber("9876543210");
            outpass.setEmergencyContactRelation("Father");
            outpass.setStatus(OutpassStatus.APPROVED);
            outpass.setCreatedAt(LocalDateTime.now());
            
            // Add warden approval details
            List<Warden> wardens = wardenRepository.findAll();
            if (!wardens.isEmpty()) {
                Warden warden = wardens.get(0);
                outpass.setReviewedBy(warden);
                outpass.setWardenComments("Approved for medical checkup");
                outpass.setReviewedAt(LocalDateTime.now());
            }
            
            Outpass savedOutpass = outpassRepository.save(outpass);
            
            System.out.println("✅ Test approved outpass created with ID: " + savedOutpass.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Test approved outpass created successfully. ID: " + savedOutpass.getId()));
        } catch (Exception e) {
            System.err.println("❌ Error creating test outpass: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Inner class for security profile response
     */
    public static class SecurityProfileResponse {
        private Long id;
        private String name;
        private String username;
        private String securityId;
        private String email;
        private String phone;
        private String shift;
        private String gateAssigned;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getSecurityId() { return securityId; }
        public void setSecurityId(String securityId) { this.securityId = securityId; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }
        
        public String getGateAssigned() { return gateAssigned; }
        public void setGateAssigned(String gateAssigned) { this.gateAssigned = gateAssigned; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    /**
     * Inner class for departure request
     */
    public static class DepartureRequest {
        private String comments;
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
    
    /**
     * Inner class for return request
     */
    public static class ReturnRequest {
        private String comments;
        private String lateReturnReason;
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        
        public String getLateReturnReason() { return lateReturnReason; }
        public void setLateReturnReason(String lateReturnReason) { this.lateReturnReason = lateReturnReason; }
    }
    
    /**
     * Inner class for security dashboard data
     */
    public static class SecurityDashboardData {
        private int approvedOutpasses;
        private int activeOutpasses;
        private int completedToday;
        private int lateReturns;
        private List<OutpassResponse> pendingDepartures;
        private List<OutpassResponse> pendingReturns;
        private List<OutpassResponse> recentActivity;
        
        // Getters and Setters
        public int getApprovedOutpasses() { return approvedOutpasses; }
        public void setApprovedOutpasses(int approvedOutpasses) { this.approvedOutpasses = approvedOutpasses; }
        
        public int getActiveOutpasses() { return activeOutpasses; }
        public void setActiveOutpasses(int activeOutpasses) { this.activeOutpasses = activeOutpasses; }
        
        public int getCompletedToday() { return completedToday; }
        public void setCompletedToday(int completedToday) { this.completedToday = completedToday; }
        
        public int getLateReturns() { return lateReturns; }
        public void setLateReturns(int lateReturns) { this.lateReturns = lateReturns; }
        
        public List<OutpassResponse> getPendingDepartures() { return pendingDepartures; }
        public void setPendingDepartures(List<OutpassResponse> pendingDepartures) { this.pendingDepartures = pendingDepartures; }
        
        public List<OutpassResponse> getPendingReturns() { return pendingReturns; }
        public void setPendingReturns(List<OutpassResponse> pendingReturns) { this.pendingReturns = pendingReturns; }
        
        public List<OutpassResponse> getRecentActivity() { return recentActivity; }
        public void setRecentActivity(List<OutpassResponse> recentActivity) { this.recentActivity = recentActivity; }
    }
    
    /**
     * Inner class for today's activity
     */
    public static class TodayActivity {
        private List<OutpassResponse> departuresToday;
        private List<OutpassResponse> returnsToday;
        private List<OutpassResponse> expectedReturns;
        
        // Getters and Setters
        public List<OutpassResponse> getDeparturesToday() { return departuresToday; }
        public void setDeparturesToday(List<OutpassResponse> departuresToday) { this.departuresToday = departuresToday; }
        
        public List<OutpassResponse> getReturnsToday() { return returnsToday; }
        public void setReturnsToday(List<OutpassResponse> returnsToday) { this.returnsToday = returnsToday; }
        
        public List<OutpassResponse> getExpectedReturns() { return expectedReturns; }
        public void setExpectedReturns(List<OutpassResponse> expectedReturns) { this.expectedReturns = expectedReturns; }
    }
}