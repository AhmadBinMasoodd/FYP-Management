package com.company.fyp_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.company.fyp_management.entity.Grades;
import com.company.fyp_management.entity.Student;

import java.util.Optional;

public interface GradesRepository extends JpaRepository<Grades, Integer> {
	@Query("SELECT g FROM Grades g WHERE g.student.student_id = :studentId LIMIT 1")
	Grades findByStudentId(@Param("studentId") Integer studentId);
	
	// Find grade by student entity
	Optional<Grades> findByStudent(Student student);
}