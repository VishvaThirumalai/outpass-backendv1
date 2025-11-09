// In OutpassRepository.java - Fix the queries
package com.mit.outpass.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Student;
import com.mit.outpass.enums.OutpassStatus;

@Repository
public interface OutpassRepository extends JpaRepository<Outpass, Long> {
    
    // Find outpasses by student
    List<Outpass> findByStudent(Student student);
    
    // Find outpasses by student ID
    List<Outpass> findByStudentId(Long studentId);
    
    // Find outpasses by status - USE THIS FOR APPROVED
    List<Outpass> findByStatus(OutpassStatus status);
    
    // Find outpasses by student and status
    List<Outpass> findByStudentAndStatus(Student student, OutpassStatus status);
    
    // Find outpasses by student ID and status
    List<Outpass> findByStudentIdAndStatus(Long studentId, OutpassStatus status);
    
    // Find pending outpasses
    @Query("SELECT o FROM Outpass o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<Outpass> findPendingOutpasses();
    
    // Find approved outpasses - FIXED: Use enum value directly
    @Query("SELECT o FROM Outpass o WHERE o.status = com.mit.outpass.enums.OutpassStatus.APPROVED ORDER BY o.leaveStartDate ASC")
    List<Outpass> findApprovedOutpasses();
    
    // Find active outpasses (students currently out) - FIXED
    @Query("SELECT o FROM Outpass o WHERE o.status = com.mit.outpass.enums.OutpassStatus.ACTIVE ORDER BY o.actualDepartureTime ASC")
    List<Outpass> findActiveOutpasses();
    
    // Find outpasses by date range
    @Query("SELECT o FROM Outpass o WHERE o.leaveStartDate BETWEEN :startDate AND :endDate")
    List<Outpass> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    // Find outpasses created between dates
    @Query("SELECT o FROM Outpass o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Outpass> findByCreatedDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Find expired outpasses
    @Query("SELECT o FROM Outpass o WHERE o.expectedReturnDate < :currentTime AND " +
           "(o.status = 'APPROVED' OR o.status = 'ACTIVE') ORDER BY o.expectedReturnDate ASC")
    List<Outpass> findExpiredOutpasses(@Param("currentTime") LocalDateTime currentTime);
    
    // Find outpasses requiring return today
    @Query("SELECT o FROM Outpass o WHERE DATE(o.expectedReturnDate) = DATE(:today) AND o.status = 'ACTIVE'")
    List<Outpass> findOutpassesReturningToday(@Param("today") LocalDateTime today);
    
    // Find outpasses starting today
    @Query("SELECT o FROM Outpass o WHERE DATE(o.leaveStartDate) = DATE(:today) AND o.status = 'APPROVED'")
    List<Outpass> findOutpassesStartingToday(@Param("today") LocalDateTime today);
    
    // Find late returns
    @Query("SELECT o FROM Outpass o WHERE o.isLateReturn = true ORDER BY o.actualReturnTime DESC")
    List<Outpass> findLateReturns();
    
    // Count outpasses by status
    @Query("SELECT COUNT(o) FROM Outpass o WHERE o.status = :status")
    long countByStatus(@Param("status") OutpassStatus status);
    
    // Count student's outpasses by status
    @Query("SELECT COUNT(o) FROM Outpass o WHERE o.student.id = :studentId AND o.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") OutpassStatus status);
    
    // Find student's recent outpasses (last N)
    @Query("SELECT o FROM Outpass o WHERE o.student.id = :studentId ORDER BY o.createdAt DESC")
    List<Outpass> findRecentOutpassesByStudentId(@Param("studentId") Long studentId);
    
    // Check if student has any active or pending outpass
    @Query("SELECT COUNT(o) > 0 FROM Outpass o WHERE o.student.id = :studentId AND " +
           "(o.status = 'PENDING' OR o.status = 'APPROVED' OR o.status = 'ACTIVE')")
    boolean hasActiveOrPendingOutpass(@Param("studentId") Long studentId);
    
    // Find outpasses by hostel name (through student)
    @Query("SELECT o FROM Outpass o WHERE o.student.hostelName = :hostelName ORDER BY o.createdAt DESC")
    List<Outpass> findByStudentHostelName(@Param("hostelName") String hostelName);
    
    // Find outpasses by course (through student)
    @Query("SELECT o FROM Outpass o WHERE o.student.course = :course ORDER BY o.createdAt DESC")
    List<Outpass> findByStudentCourse(@Param("course") String course);
    
    // Find outpasses with specific warden review
    @Query("SELECT o FROM Outpass o WHERE o.reviewedBy.id = :wardenId ORDER BY o.reviewedAt DESC")
    List<Outpass> findByReviewedByWardenId(@Param("wardenId") Long wardenId);
    
    // Find outpasses processed by security
    @Query("SELECT o FROM Outpass o WHERE o.departureMarkedBy.id = :securityId OR o.returnMarkedBy.id = :securityId " +
           "ORDER BY o.actualDepartureTime DESC")
    List<Outpass> findBySecurityId(@Param("securityId") Long securityId);
    
    // Statistics queries
    @Query("SELECT o.status, COUNT(o) FROM Outpass o GROUP BY o.status")
    List<Object[]> getOutpassStatusStatistics();
    
    @Query("SELECT s.hostelName, COUNT(o) FROM Outpass o JOIN o.student s GROUP BY s.hostelName")
    List<Object[]> getOutpassCountByHostel();
    
    @Query("SELECT FUNCTION('MONTH', o.createdAt), COUNT(o) FROM Outpass o " +
           "WHERE FUNCTION('YEAR', o.createdAt) = :year GROUP BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getMonthlyOutpassCount(@Param("year") int year);
   // Add these methods to OutpassRepository.java

@Query("SELECT o FROM Outpass o WHERE o.student.hostelName = :hostelName AND o.status = 'PENDING' ORDER BY o.createdAt ASC")
List<Outpass> findPendingOutpassesByHostel(@Param("hostelName") String hostelName);

@Query("SELECT o FROM Outpass o WHERE o.student.hostelName = :hostelName AND o.status = com.mit.outpass.enums.OutpassStatus.APPROVED ORDER BY o.leaveStartDate ASC")
List<Outpass> findApprovedOutpassesByHostel(@Param("hostelName") String hostelName);

@Query("SELECT o FROM Outpass o WHERE o.student.hostelName = :hostelName AND o.status = com.mit.outpass.enums.OutpassStatus.ACTIVE ORDER BY o.actualDepartureTime ASC")
List<Outpass> findActiveOutpassesByHostel(@Param("hostelName") String hostelName);

@Query("SELECT o FROM Outpass o WHERE o.student.hostelName = :hostelName ORDER BY o.createdAt DESC")
List<Outpass> findAllOutpassesByHostel(@Param("hostelName") String hostelName);

@Query("SELECT COUNT(o) FROM Outpass o WHERE o.status = :status AND o.student.hostelName = :hostelName")
long countByStatusAndHostel(@Param("status") OutpassStatus status, @Param("hostelName") String hostelName);

@Query("SELECT COUNT(o) FROM Outpass o WHERE o.student.hostelName = :hostelName")
long countAllOutpassesByHostel(@Param("hostelName") String hostelName);

@Query("SELECT o FROM Outpass o WHERE o.reviewedBy.id = :wardenUserId")
List<Outpass> findByReviewedByWardenUserId(@Param("wardenUserId") Long wardenUserId);
}