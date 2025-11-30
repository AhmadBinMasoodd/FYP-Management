package com.company.fyp_management.entity;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "document_types")
public class DocumentTypes {
    @Id
    @Column(unique = true, nullable = false)
    private String doc_type;

    @Column(nullable = false)
    private LocalDate deadline_date;

    // getters and setters
    public String getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(String doc_type) {
        this.doc_type = doc_type;
    }

    public LocalDate getDeadline_date() {
        return deadline_date;
    }

    public void setDeadline_date(LocalDate deadline_date) {
        this.deadline_date = deadline_date;
    }
    
}
