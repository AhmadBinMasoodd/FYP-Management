package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class FileSubmission {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer file_id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @CreationTimestamp
    private LocalDateTime submission_datetime;

    // Getters and setters
    public Integer getFile_id() {
        return file_id;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDateTime getSubmission_datetime() {
        return submission_datetime;
    }
}
