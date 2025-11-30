package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
@EntityListeners(PasswordEncodingListener.class) // NEW: ensure password encoding before save/update
public class Student {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer student_id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "supervisor_id", nullable = false)
    private Integer supervisorId = -1;

    @Column(nullable = false)
    private boolean proposal = true; // true means can submit proposal

    @Column(nullable = false)
    private boolean design_document = true;

    @Column(nullable = false)
    private boolean test_document = true;

    @Column(nullable = false)
    private boolean thesis = true;

    @CreationTimestamp
    private LocalDateTime registration_date;


    // Getters and setters
    public String getId() {
        return "STU-" + String.valueOf(student_id);
    }

    public Integer getNumericId() {
        return student_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Integer supervisorId) {
        this.supervisorId = supervisorId;
    }

    public boolean getProposal() {
        return proposal;
    }

    public void setProposal(boolean proposal) {
        this.proposal = proposal;
    }

    public boolean getDesignDocument() {
        return design_document;
    }

    public void setDesignDocument(boolean design_document) {
        this.design_document = design_document;
    }

    public boolean getTestDocument() {
        return test_document;
    }

    public void setTestDocument(boolean test_document) {
        this.test_document = test_document;
    }

    public boolean getThesis() {
        return thesis;
    }

    public void setThesis(boolean thesis) {
        this.thesis = thesis;
    }

    public LocalDateTime getRegistrationDate() {
        return registration_date;
    }

}
