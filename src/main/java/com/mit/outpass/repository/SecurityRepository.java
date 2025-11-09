package com.mit.outpass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mit.outpass.entity.Security;

/**
 * Repository interface for Security entity
 */
@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {
    
    // Find security personnel by security ID
    Optional<Security> findBySecurityId(String securityId);
    
    // Find security personnel by username (inherited from User)
    Optional<Security> findByUsername(String username);
    
    // Find security personnel by shift
    List<Security> findByShift(String shift);
    
    // Find security personnel by gate assigned
    List<Security> findByGateAssigned(String gateAssigned);
    
    // Find security personnel by supervisor name
    List<Security> findBySupervisorName(String supervisorName);
    
    // Find security personnel by shift and gate
    List<Security> findByShiftAndGateAssigned(String shift, String gateAssigned);
    
    // Check if security ID exists
    boolean existsBySecurityId(String securityId);
    
    // Find security personnel with minimum years of service
    @Query("SELECT s FROM Security s WHERE s.yearsOfService >= :years")
    List<Security> findByMinimumYearsOfService(@Param("years") Integer years);
    
    // Find security personnel by partial name match (case insensitive)
    @Query("SELECT s FROM Security s WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Security> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    // Count security personnel by shift
    @Query("SELECT COUNT(s) FROM Security s WHERE s.shift = :shift")
    long countByShift(@Param("shift") String shift);
    
    // Count security personnel by gate assigned
    @Query("SELECT COUNT(s) FROM Security s WHERE s.gateAssigned = :gate")
    long countByGateAssigned(@Param("gate") String gate);
    
    // Get all distinct shifts
    @Query("SELECT DISTINCT s.shift FROM Security s ORDER BY s.shift")
    List<String> findDistinctShifts();
    
    // Get all distinct gates assigned
    @Query("SELECT DISTINCT s.gateAssigned FROM Security s WHERE s.gateAssigned IS NOT NULL ORDER BY s.gateAssigned")
    List<String> findDistinctGatesAssigned();
    
    // Find active security personnel by shift for current duty
    @Query("SELECT s FROM Security s WHERE s.shift = :shift AND s.isActive = true")
    List<Security> findActiveSecurityByShift(@Param("shift") String shift);

    @Modifying
@Query(value = "DELETE FROM security_personnel WHERE user_id = :userId", nativeQuery = true)
void deleteSecurityByIdNative(@Param("userId") Long userId);
}