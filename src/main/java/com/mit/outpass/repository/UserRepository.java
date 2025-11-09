// UserRepository.java - Add these missing methods
package com.mit.outpass.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mit.outpass.entity.User;
import com.mit.outpass.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndRole(String username, UserRole role);
    List<User> findByRole(UserRole role);
    List<User> findByIsActive(Boolean isActive);
    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :sinceDate ORDER BY u.lastLogin DESC")
    List<User> findRecentlyLoggedInUsers(@Param("sinceDate") LocalDateTime sinceDate);
    
    // Add these missing methods
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.role = :role")
    Optional<User> findByIdAndRole(@Param("id") Long id, @Param("role") UserRole role);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :id")
    int updateUserStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Modifying
@Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
void deleteUserByIdNative(@Param("userId") Long userId);
}