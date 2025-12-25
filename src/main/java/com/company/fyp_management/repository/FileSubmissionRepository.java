package com.company.fyp_management.repository;

import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileSubmissionRepository extends JpaRepository<FileSubmission, Integer> {
	// Fetch all submissions for a student (by numeric student_id) sorted by submission_datetime desc
	@Query("SELECT f FROM FileSubmission f WHERE f.student.student_id = :studentId ORDER BY f.submission_datetime DESC")
	List<FileSubmission> findAllByStudentIdOrderBySubmissionDatetimeDesc(@Param("studentId") Integer studentId);
	
	// Find all submissions by student entity
	List<FileSubmission> findByStudent(Student student);

	// Return the most recent submission for a student and doc_type — traverse the documentType relationship
	@Query("""
		SELECT f
		FROM FileSubmission f
		WHERE f.student.student_id = :studentId
		AND f.documentType.doc_type = :docType
		ORDER BY f.submission_datetime DESC
		LIMIT 1
	""")
	FileSubmission findLatestSubmission(
			@Param("studentId") Integer studentId,
			@Param("docType") String docType
	);

	// Get all approved submissions (for Evaluation Committee) - eagerly fetch student data
	@Query("SELECT f FROM FileSubmission f JOIN FETCH f.student WHERE f.is_approved = true ORDER BY f.submission_datetime DESC")
	List<FileSubmission> findAllApprovedSubmissions();

	// Get approved submissions for a specific student
	@Query("SELECT f FROM FileSubmission f JOIN FETCH f.student WHERE f.student.student_id = :studentId AND f.is_approved = true ORDER BY f.submission_datetime DESC")
	List<FileSubmission> findApprovedSubmissionsByStudentId(@Param("studentId") Integer studentId);
}

