package com.company.fyp_management.entity;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Assignment {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assignment_id;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty; // created by

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private LocalDate deadline_date;

    // Getters and setters
    public Integer getAssignment_id() {
        return assignment_id;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    // Allow setting the faculty (e.g. when creating/updating an assignment)
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline_date() {
        return deadline_date;
    }

    public void setDeadline_date(LocalDate deadline_date) {
        if(deadline_date != null && deadline_date.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
        this.deadline_date = deadline_date;
    }
}
