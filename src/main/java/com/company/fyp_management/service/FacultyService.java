package com.company.fyp_management.service;

import com.company.fyp_management.entity.Faculty;
import com.company.fyp_management.repository.FacultyRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

@Service
public class FacultyService {
    
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Optional<Faculty> getFacultyById(Integer id) {
        return facultyRepository.findById(id);
    }

}
