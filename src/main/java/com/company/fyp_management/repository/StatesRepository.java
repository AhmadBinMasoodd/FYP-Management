package com.company.fyp_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.company.fyp_management.entity.State;


public interface StatesRepository extends JpaRepository<State, String> {

}