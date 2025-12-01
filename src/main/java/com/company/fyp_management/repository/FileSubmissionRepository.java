package com.company.fyp_management.repository;

import com.company.fyp_management.entity.FileSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileSubmissionRepository extends JpaRepository<FileSubmission, Integer> {
	// Fetch all submissions for a student (by numeric student_id) sorted by submission_datetime desc
	@Query("SELECT f FROM FileSubmission f WHERE f.student.student_id = :studentId ORDER BY f.submission_datetime DESC")
	List<FileSubmission> findAllByStudentIdOrderBySubmissionDatetimeDesc(@Param("studentId") Integer studentId);

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
}

