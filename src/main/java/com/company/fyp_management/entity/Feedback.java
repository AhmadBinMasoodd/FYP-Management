package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedback_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private FileSubmission submission;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submitted_at;

    // Getters and setters
    public Integer getId() {
        return feedback_id;
    }

    // expose FileSubmission entity
    public FileSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(FileSubmission submission) {
        this.submission = submission;
    }

    // convenience: return the submission's primary key (null if unavailable)
    public Integer getSubmissionId() {
        if (this.submission == null) return null;
        try {
            Object idObj = this.submission.getClass().getMethod("getId").invoke(this.submission);
            if (idObj == null) return null;
            if (idObj instanceof Number) return ((Number) idObj).intValue();
            return Integer.valueOf(idObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSubmittedAt() {
        return submitted_at;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submitted_at = submittedAt;
    }
    
}
