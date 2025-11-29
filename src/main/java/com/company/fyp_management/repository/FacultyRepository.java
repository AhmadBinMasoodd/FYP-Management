package com.company.fyp_management.repository;

import com.company.fyp_management.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {
    Faculty findByIdAndPassword(String id, String password);
}
