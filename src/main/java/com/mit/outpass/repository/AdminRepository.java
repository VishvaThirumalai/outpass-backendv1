// AdminRepository.java
package com.mit.outpass.repository;

import com.mit.outpass.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Since Admin extends User, we can use the inherited id directly
    // This will find Admin by the primary key (which is the same as user_id)
    Optional<Admin> findById(Long id);
    
    // If you need to find by admin_id (the custom admin ID field)
    Optional<Admin> findByAdminId(String adminId);
    Optional<Admin> findByUsername(String username);
    boolean existsByAdminId(String adminId);
    @Modifying
@Query(value = "DELETE FROM admins WHERE user_id = :userId", nativeQuery = true)
void deleteAdminByIdNative(@Param("userId") Long userId);
    // REMOVE THIS LINE - it's causing the error
    // Optional<Admin> findByUserId(Long userId);
}