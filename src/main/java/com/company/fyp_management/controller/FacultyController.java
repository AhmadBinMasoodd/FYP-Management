package com.company.fyp_management.controller;
import com.company.fyp_management.entity.Faculty;
import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.entity.Student;
import com.company.fyp_management.service.FacultyService;
import com.company.fyp_management.service.StudentService;
import com.company.fyp_management.service.FileSubmissionService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;
    private final StudentService studentService;
    private final FileSubmissionService fileSubmissionService;

    public FacultyController(FacultyService facultyService, StudentService studentService, FileSubmissionService fileSubmissionService) {
        this.facultyService = facultyService;
        this.studentService = studentService;
        this.fileSubmissionService = fileSubmissionService;
    }

    @PostMapping("/register")
    public Faculty registerFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }

    @GetMapping("/mystudents")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public java.util.List<Student> getMyStudents(HttpSession session) {
        // resolve userId from session
        Object uid = session.getAttribute("userId");
        if (uid == null) {
            throw new IllegalArgumentException("No authenticated user in session");
        }
        Integer userId;
        if (uid instanceof Number) {
            userId = ((Number) uid).intValue();
        } else {
            try {
                userId = Integer.parseInt(uid.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid userId in session");
            }
        }

        List<Student> students = studentService.getStudentsBySupervisorId(userId);
        return students;
    }

    @GetMapping("/unassignedstudents")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public java.util.List<Student> getUnassignedStudents() {
        List<Student> students = studentService.getStudentsBySupervisorId(-1);
        return students;
    }

    @PostMapping("/assignstudent")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Student assignStudent(@RequestBody Student student, HttpSession session) {
        // resolve userId from session
        Object uid = session.getAttribute("userId");
        if (uid == null) {
            throw new IllegalArgumentException("No authenticated user in session");
        }
        Integer userId;
        if (uid instanceof Number) {
            userId = ((Number) uid).intValue();
        } else {
            try {
                userId = Integer.parseInt(uid.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid userId in session");
            }
        }

        Integer studentId = student.getNumericId();
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required");
        }

        Optional<Student> persistedOpt = studentService.getStudentById(studentId);
        if (persistedOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }

        Student persistedStudent = persistedOpt.get();
        Integer currentSupervisor = persistedStudent.getSupervisorId();
        if (currentSupervisor != null && currentSupervisor.intValue() != -1) {
            throw new IllegalStateException("Supervisor already assigned");
        }

        // assign supervisor and persist
        persistedStudent.setSupervisorId(userId);
        Student updated = studentService.updateStudent(persistedStudent);
        return updated;
    }

    @PostMapping("/approvesubmission")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public FileSubmission approveSubmission(@RequestBody FileSubmission fileSubmission, HttpSession session) {
        // resolve userId from session
        Object uid = session.getAttribute("userId");
        if (uid == null) {
            throw new IllegalArgumentException("No authenticated user in session");
        }
        Integer userId;
        if (uid instanceof Number) {
            userId = ((Number) uid).intValue();
        } else {
            try {
                userId = Integer.parseInt(uid.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid userId in session");
            }
        }

        Integer fsId = fileSubmission.getFile_id();
        if (fsId == null) {
            throw new IllegalArgumentException("File submission id is required");
        }

        // Service returns FileSubmission (not Optional) — check for null
        FileSubmission persisted = fileSubmissionService.getFileSubmissionById(fsId);
        if (persisted == null) {
            throw new IllegalArgumentException("File submission not found");
        }

        Student temp = persisted.getStudent();
        if (temp == null) {
            throw new IllegalStateException("File submission has no associated student");
        }
        Integer studentId = temp.getNumericId();
        if (studentId == null) {
            throw new IllegalStateException("File submission has no associated student");
        }

        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        Student student = studentOpt.get();

        Integer supervisorId = student.getSupervisorId();
        if (supervisorId == null || supervisorId.intValue() != userId.intValue()) {
            throw new SecurityException("Not authorized to approve this submission");
        }

        // mark approved and persist
        persisted.setIs_approved(true);
        FileSubmission updated = fileSubmissionService.updateFileSubmission(persisted);
        return updated;
    }

    @PostMapping("/requestrevision")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Student requestRevision(@RequestBody FileSubmission fileSubmission, HttpSession session) {
        // resolve userId from session
        Object uid = session.getAttribute("userId");
        if (uid == null) {
            throw new IllegalArgumentException("No authenticated user in session");
        }
        Integer userId;
        if (uid instanceof Number) {
            userId = ((Number) uid).intValue();
        } else {
            try {
                userId = Integer.parseInt(uid.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid userId in session");
            }
        }

        Integer fsId = fileSubmission.getFile_id();
        if (fsId == null) {
            throw new IllegalArgumentException("File submission id is required");
        }

        // Service returns FileSubmission (not Optional) — check for null
        FileSubmission persisted = fileSubmissionService.getFileSubmissionById(fsId);
        if (persisted == null) {
            throw new IllegalArgumentException("File submission not found");
        }

        Student temp = persisted.getStudent();
        if (temp == null) {
            throw new IllegalStateException("File submission has no associated student");
        }
        Integer studentId = temp.getNumericId();
        if (studentId == null) {
            throw new IllegalStateException("File submission has no associated student");
        }

        DocumentTypes docType = persisted.getdocumentType();
        if (docType == null) {
            throw new IllegalStateException("File submission has no associated document type");
        }
        String doc_type = docType.getDoc_type();
        if (doc_type == null) {
            throw new IllegalStateException("File submission has invalid document type");
        }
        
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        Student student = studentOpt.get();

        Integer supervisorId = student.getSupervisorId();
        if (supervisorId == null || supervisorId.intValue() != userId.intValue()) {
            throw new SecurityException("Not authorized to Request Revision for this submission");
        }

        if (doc_type.equals("Proposal")) {
            student.setProposal(true);
        } else if (doc_type.equals("Design Document")) {
            student.setDesignDocument(true);
        } else if (doc_type.equals("Test Document")) {
            student.setTestDocument(true);
        } else if (doc_type.equals("Thesis")) {
            student.setThesis(true);
        } else {
            throw new IllegalStateException("Unknown document type: " + doc_type);
        }
        
        Student updated = studentService.updateStudent(student);
        return updated;
    }

}
