package com.company.fyp_management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "states")
public class State {
    @Id
    @Column(unique = true, nullable = false)
    private String state_name;

    @Column(nullable = false)
    private boolean value;


    // getters and setters
    public String getstateName() {
        return state_name;
    }

    public void setstateName(String state_name) {
        this.state_name = state_name;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
