package com.company.fyp_management.repository;

import com.company.fyp_management.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
	// Fetch all feedbacks for a given submission (by numeric submission_id) sorted by submittedAt desc
	@Query("SELECT f FROM Feedback f WHERE f.submission.file_id = :submissionId ORDER BY f.submitted_at DESC")
	List<Feedback> findAllBySubmissionIdOrderBySubmittedAtDesc(@Param("submissionId") Integer submissionId);

	// Delete all feedbacks for a given submission
	@org.springframework.data.jpa.repository.Modifying
	@Query("DELETE FROM Feedback f WHERE f.submission.file_id = :submissionId")
	void deleteAllBySubmissionId(@Param("submissionId") Integer submissionId);
}