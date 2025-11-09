package com.mit.outpass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mit.outpass.entity.Outpass;
import com.mit.outpass.entity.Warden;

/**
 * Repository interface for Warden entity
 */
@Repository
public interface WardenRepository extends JpaRepository<Warden, Long> {
    
    // Find warden by employee ID
    Optional<Warden> findByEmployeeId(String employeeId);
    
    // Find warden by username (inherited from User)
    Optional<Warden> findByUsername(String username);
    
    // Find wardens by department
    List<Warden> findByDepartment(String department);
    
    // Find wardens by designation
    List<Warden> findByDesignation(String designation);
    
    // Find wardens by hostel assigned
    List<Warden> findByHostelAssigned(String hostelAssigned);
    
    // Find wardens by department and designation
    List<Warden> findByDepartmentAndDesignation(String department, String designation);
    
    // Check if employee ID exists
    boolean existsByEmployeeId(String employeeId);
    
    // Find wardens with experience greater than specified years
    @Query("SELECT w FROM Warden w WHERE w.yearsOfExperience >= :years")
    List<Warden> findByMinimumExperience(@Param("years") Integer years);
    
    // Find wardens by partial name match (case insensitive)
    @Query("SELECT w FROM Warden w WHERE LOWER(w.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Warden> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    // Count wardens by department
    @Query("SELECT COUNT(w) FROM Warden w WHERE w.department = :department")
    long countByDepartment(@Param("department") String department);
    
    // Count wardens by hostel assigned
    @Query("SELECT COUNT(w) FROM Warden w WHERE w.hostelAssigned = :hostelName")
    long countByHostelAssigned(@Param("hostelName") String hostelName);
    
    // Get all distinct departments
    @Query("SELECT DISTINCT w.department FROM Warden w ORDER BY w.department")
    List<String> findDistinctDepartments();
    
    // Get all distinct designations
    @Query("SELECT DISTINCT w.designation FROM Warden w ORDER BY w.designation")
    List<String> findDistinctDesignations();
    
    // Get all distinct hostels assigned
    @Query("SELECT DISTINCT w.hostelAssigned FROM Warden w WHERE w.hostelAssigned IS NOT NULL ORDER BY w.hostelAssigned")
    List<String> findDistinctHostelsAssigned();
    @Query("SELECT o FROM Outpass o WHERE o.reviewedBy.id = :wardenUserId")
List<Outpass> findByReviewedByUserId(@Param("wardenUserId") Long wardenUserId);
@Modifying
@Query(value = "DELETE FROM wardens WHERE user_id = :userId", nativeQuery = true)
void deleteWardenByIdNative(@Param("userId") Long userId);
}