package com.company.fyp_management.service;

import com.company.fyp_management.entity.Assignment;
import com.company.fyp_management.repository.AssignmentRepository;

public class AssignmentService {
    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public Assignment getAssignmentById(Integer id) {
        return assignmentRepository.findById(id).orElse(null);
    }
    
}
