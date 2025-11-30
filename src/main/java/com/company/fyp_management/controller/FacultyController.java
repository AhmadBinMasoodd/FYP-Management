package com.company.fyp_management.controller;
import com.company.fyp_management.entity.Faculty;
import com.company.fyp_management.service.FacultyService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping("/register")
    public Faculty registerFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }
}
