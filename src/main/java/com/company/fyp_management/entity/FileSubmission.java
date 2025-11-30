package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "file_submissions")
public class FileSubmission {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer file_id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String filename;

    @ManyToOne
    @JoinColumn(name = "doc_type", referencedColumnName = "doc_type", nullable = false)
    private DocumentTypes documentType;

    @Column(nullable = false)
    private boolean is_approved = false;

    @Column(nullable = false)
    private boolean submitted_late;

    @CreationTimestamp
    private LocalDateTime submission_datetime;

    // Getters and setters
    public Integer getFile_id() {
        return file_id;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public DocumentTypes getdocumentType() {
        return documentType;
    }

    public void setdocumentType(DocumentTypes documentType) {
        this.documentType = documentType;
    }

    public String getDoc_type() {
        return documentType == null ? null : documentType.getDoc_type();
    }

    public boolean getIs_approved() {
        return is_approved;
    }

    public void setIs_approved(boolean is_approved) {
        this.is_approved = is_approved;
    }

    public boolean getSubmitted_late() {
        return submitted_late;
    }

    public void setSubmitted_late(boolean submitted_late) {
        this.submitted_late = submitted_late;
    }

    public LocalDateTime getSubmission_datetime() {
        return submission_datetime;
    }

    // ensure submitted_late is computed before persist/update
    @PrePersist
    public void prePersistComputeLate() {
        if (this.documentType == null) return;
        LocalDate deadline = this.documentType.getDeadline_date();
        if (deadline == null) return;
        // on persist, submission_datetime may not be populated yet; use current date
        LocalDate submitDate = LocalDate.now();
        this.submitted_late = submitDate.isAfter(deadline);
    }

    @PreUpdate
    public void preUpdateComputeLate() {
        if (this.documentType == null) return;
        LocalDate deadline = this.documentType.getDeadline_date();
        if (deadline == null) return;
        // prefer actual submission_datetime when available
        LocalDate submitDate = (this.submission_datetime != null)
                ? this.submission_datetime.toLocalDate()
                : LocalDate.now();
        this.submitted_late = submitDate.isAfter(deadline);
    }
}
