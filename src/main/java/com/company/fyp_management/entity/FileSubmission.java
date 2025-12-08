package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
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

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
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

    // Include student_id in JSON response (even though student object is @JsonIgnore)
    public Integer getStudent_id() {
        return student == null ? null : student.getNumericId();
    }

    // Include student name in JSON response
    public String getStudent_name() {
        return student == null ? null : student.getName();
    }

    // Include student email in JSON response
    public String getStudent_email() {
        return student == null ? null : student.getEmail();
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
