package com.mit.outpass.service;

import com.mit.outpass.entity.Student;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }
    
    public Student getStudentByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "rollNumber", rollNumber));
    }
    
    public Student getStudentByUsername(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));
    }
    
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    public List<Student> getStudentsByHostel(String hostelName) {
        return studentRepository.findByHostelName(hostelName);
    }
    
    public List<Student> getStudentsByCourse(String course) {
        return studentRepository.findByCourse(course);
    }
    
    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }
}