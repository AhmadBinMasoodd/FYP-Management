package com.company.fyp_management.service;

import com.company.fyp_management.entity.Feedback;
import com.company.fyp_management.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {
	private final FeedbackRepository feedbackRepository;

	public FeedbackService(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	public List<Feedback> getFeedbacksBySubmissionId(Integer submissionId) {
		return feedbackRepository.findAllBySubmissionIdOrderBySubmittedAtDesc(submissionId);
	}
}
