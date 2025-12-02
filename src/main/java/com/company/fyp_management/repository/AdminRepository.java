package com.company.fyp_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.company.fyp_management.entity.Admin;


public interface AdminRepository extends JpaRepository<Admin, Integer> {

}