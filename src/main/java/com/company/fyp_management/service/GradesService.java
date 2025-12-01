package com.company.fyp_management.service;

import com.company.fyp_management.entity.Grades;
import com.company.fyp_management.repository.GradesRepository;
import org.springframework.stereotype.Service;


@Service
public class GradesService {
	private final GradesRepository gradesRepository;

	public GradesService(GradesRepository gradesRepository) {
		this.gradesRepository = gradesRepository;
	}

    public Grades createGrades(Grades grades) {
        return gradesRepository.save(grades);
    }

    public Grades getGradesById(Integer gradeId) {
        return gradesRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grades not found with id: " + gradeId));
    }

    public Grades getGradesByStudentId(Integer studentId) {
        Grades grades = gradesRepository.findByStudentId(studentId);
        if (grades == null) {
            throw new IllegalArgumentException("Grades not found for student id: " + studentId);
        }
        return grades;
    }

    public Grades updateGrades(Grades grades) {
        return gradesRepository.save(grades);
    }

}

