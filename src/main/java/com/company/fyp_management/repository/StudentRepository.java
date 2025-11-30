package com.company.fyp_management.repository;

import com.company.fyp_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    // Find a student by id (string id field) and password. Returns null if not found.
    Student findByIdAndPassword(String id, String password);

    // Find students by a supervisor id when Student has a supervisorId field (e.g. Integer supervisorId)
    List<Student> findBySupervisorId(Integer supervisorId);
}
