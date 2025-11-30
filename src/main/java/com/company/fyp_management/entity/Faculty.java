package com.company.fyp_management.entity;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_members")
@EntityListeners(PasswordEncodingListener.class) // NEW: ensure password encoding before save/update
public class Faculty {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer faculty_id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    private LocalDateTime registration_date;


    // Getters and setters
    public String getId() {
        return "FAC-" + String.valueOf(faculty_id);
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

    // keep setter simple; encoding is handled by PasswordEncodingListener
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

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRegistrationDate() {
        return registration_date;
    }

    public void setStatus(String status) {
    if ("Supervisor".equals(status) || 
        "Evaluation Committee Member".equals(status) || 
        "FYP Committee Member".equals(status) || 
        "Not Approved".equals(status)) {
        this.status = status;
    } else {
        this.status = "";
    }
    }
}
