package com.mit.outpass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mit.outpass.entity.Student;

/**
 * Repository interface for Student entity
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Find student by roll number
    Optional<Student> findByRollNumber(String rollNumber);
    
    // Find student by username (inherited from User)
    Optional<Student> findByUsername(String username);
    // Find students by course
    List<Student> findByCourse(String course);
    
    // Find students by degree
    List<Student> findByDegree(String degree);
    
    // Find students by hostel name
    List<Student> findByHostelName(String hostelName);
    
    // Find students by hostel name and room number
    Optional<Student> findByHostelNameAndRoomNumber(String hostelName, String roomNumber);
    
    // Find students by year of study
    List<Student> findByYearOfStudy(Integer yearOfStudy);
    
    // Find students by course and degree
    List<Student> findByCourseAndDegree(String course, String degree);
    
    // Check if roll number exists
    boolean existsByRollNumber(String rollNumber);
    
    // Check if room is occupied
    boolean existsByHostelNameAndRoomNumber(String hostelName, String roomNumber);
    
    // Find students by partial roll number match
    @Query("SELECT s FROM Student s WHERE s.rollNumber LIKE %:rollNumber%")
    List<Student> findByRollNumberContaining(@Param("rollNumber") String rollNumber);
    
    // Find students by partial name match (case insensitive)
    @Query("SELECT s FROM Student s WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    // Find students in a specific hostel and year
    @Query("SELECT s FROM Student s WHERE s.hostelName = :hostelName AND s.yearOfStudy = :year")
    List<Student> findByHostelAndYear(@Param("hostelName") String hostelName, @Param("year") Integer year);
    
    // Count students by hostel
    @Query("SELECT COUNT(s) FROM Student s WHERE s.hostelName = :hostelName")
    long countByHostelName(@Param("hostelName") String hostelName);
    
    // Count students by course
    @Query("SELECT COUNT(s) FROM Student s WHERE s.course = :course")
    long countByCourse(@Param("course") String course);
    
    // Get all distinct hostel names
    @Query("SELECT DISTINCT s.hostelName FROM Student s ORDER BY s.hostelName")
    List<String> findDistinctHostelNames();
    
    // Get all distinct courses
    @Query("SELECT DISTINCT s.course FROM Student s ORDER BY s.course")
    List<String> findDistinctCourses();
    
    // Get all distinct degrees
    @Query("SELECT DISTINCT s.degree FROM Student s ORDER BY s.degree")
    List<String> findDistinctDegrees();

    @Modifying
@Query(value = "DELETE FROM students WHERE user_id = :userId", nativeQuery = true)
void deleteStudentByIdNative(@Param("userId") Long userId);
}