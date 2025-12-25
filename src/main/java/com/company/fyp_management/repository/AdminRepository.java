package com.company.fyp_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.company.fyp_management.entity.Admin;
import java.util.Optional;


public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUsername(String username);
}