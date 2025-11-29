package com.company.fyp_management.repository;

import com.company.fyp_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    // Find a student by id (string id field) and password. Returns null if not found.
    Student findByIdAndPassword(String id, String password);
}
