package com.company.fyp_management.controller;

import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.entity.Grades;
import com.company.fyp_management.entity.Student;
import com.company.fyp_management.entity.State;
import org.springframework.web.bind.annotation.*;
import com.company.fyp_management.service.StateService;
import com.company.fyp_management.service.StudentService;
import com.company.fyp_management.service.GradesService;
import com.company.fyp_management.service.FileSubmissionService;
import com.company.fyp_management.service.FeedbackService; // added
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import com.company.fyp_management.repository.StudentRepository;
import com.company.fyp_management.repository.DocumentTypesRepository;
import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.entity.Feedback; // added
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final FileSubmissionService fileSubmissionService;
    private final StudentRepository studentRepository;             // NEW
    private final DocumentTypesRepository documentTypesRepository; // NEW
    private final FeedbackService feedbackService;                 // NEW
    private final GradesService gradesService;
    private final StateService stateService;

    // constructor updated to accept repositories and feedbackService
    public StudentController(StudentService studentService,
                            FileSubmissionService fileSubmissionService,
                            StudentRepository studentRepository,
                            DocumentTypesRepository documentTypesRepository,
                            GradesService gradesService,
                            StateService stateService,
                            FeedbackService feedbackService) { // updated
        this.studentService = studentService;
        this.fileSubmissionService = fileSubmissionService;
        this.studentRepository = studentRepository;
        this.documentTypesRepository = documentTypesRepository;
        this.feedbackService = feedbackService;
        this.gradesService = gradesService;
        this.stateService = stateService;
    }

    @PostMapping("/register")
    public Student registerStudent(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('STUDENT')") // changed: use hasRole('STUDENT'), Spring will match ROLE_STUDENT authority
    public Student getSubmissionStatus(HttpSession session) {
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

        // load student
        Optional<Student> studentOpt = studentRepository.findById(userId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found for id: " + userId);
        }
        return studentOpt.get();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')") // changed: use hasRole('STUDENT'), Spring will match ROLE_STUDENT authority
    public FileSubmission uploadFile(
            @RequestPart("metadata") FileSubmission fileSubmission,
            @RequestPart("doc_type") String docType,
            @RequestPart("file") MultipartFile file,
            HttpSession session) {

        // 1) resolve userId from session
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

        // 2) load managed Student
        Optional<Student> studentOpt = studentRepository.findById(userId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found for id: " + userId);
        }
        Student managedStudent = studentOpt.get();

        // check if student has supervisor
        if (managedStudent.getSupervisorId() == -1) {
            throw new IllegalArgumentException("Cannot submit file: student has no assigned supervisor");
        }

        // 2.a) check permission to submit this doc_type based on student's booleans
        if (!isAllowedToSubmit(managedStudent, docType)) {
            throw new IllegalArgumentException("Submission not allowed: student has already submitted this document type or is not permitted to submit it");
        }

        // attach managed student
        fileSubmission.setStudent(managedStudent);

        // 3) resolve and set document type (foreign key)
        Optional<DocumentTypes> dtOpt = documentTypesRepository.findById(docType);
        if (dtOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid doc_type: " + docType);
        }
        DocumentTypes documentTypeEntity = dtOpt.get();
        fileSubmission.setdocumentType(documentTypeEntity);

        // 4) delegate to service to store file and persist
        FileSubmission saved = fileSubmissionService.createFileSubmissionWithUpload(fileSubmission, file);

        // 5) after successful save, mark student's corresponding boolean to false and persist
        setSubmittedFlagFalse(managedStudent, docType);
        studentRepository.save(managedStudent);

        return saved;
    }

    @GetMapping("/mysubmissions")
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<SubmissionWithFeedback> getMySubmissions(HttpSession session) {
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

        // fetch submissions
        List<FileSubmission> submissions = fileSubmissionService.getSubmissionsByStudentId(userId);

        // build result pairing each submission with its feedback list (empty when none)
        List<SubmissionWithFeedback> result = new ArrayList<>();
        for (FileSubmission submission : submissions) {
            Integer submissionId = submission.getFile_id();
            List<Feedback> feedbacks = feedbackService.getFeedbacksBySubmissionId(submissionId);
            if (feedbacks == null) feedbacks = List.of();
            result.add(new SubmissionWithFeedback(submission, feedbacks));
        }
        return result;
    }

    @GetMapping("/mygrades")
    @PreAuthorize("hasRole('STUDENT')")
    public Grades getMyGrades(HttpSession session) {
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

        State current = stateService.getStateByKey("release_grades");
        if (!current.getValue()) {
            throw new IllegalArgumentException("FYP Committe has not released Grades yet.");
        }

        // fetch submissions
        Grades my_grades = gradesService.getGradesByStudentId(userId);
        return my_grades;
    }

    // helper classes
    // DTO returned as JSON: contains submission and feedback list
    public static class SubmissionWithFeedback {
        private final FileSubmission submission;
        private final List<Feedback> feedbacks;

        public SubmissionWithFeedback(FileSubmission submission, List<Feedback> feedbacks) {
            this.submission = submission;
            this.feedbacks = feedbacks;
        }

        public FileSubmission getSubmission() {
            return submission;
        }

        public List<Feedback> getFeedbacks() {
            return feedbacks;
        }
    }

    // helper functions
    // helper to normalize docType consistently across checks
    private String normalizeDocType(String docType) {
        if (docType == null) return "";
        // lowercase and remove all non-alphanumeric characters
        return docType.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    // helper to normalize docType and check corresponding boolean
    private boolean isAllowedToSubmit(Student s, String docType) {
        if (docType == null) return false;
        String norm = normalizeDocType(docType);
        switch (norm) {
            case "proposal":
                return s.getProposal();
            case "designdocument":
                return s.getDesignDocument();
            case "testdocument":
                return s.getTestDocument();
            case "thesis":
                return s.getThesis();
            default:
                // unknown doc type - reject
                return false;
        }
    }

    // helper to set the appropriate boolean to false after submission
    private void setSubmittedFlagFalse(Student s, String docType) {
        if (docType == null) return;
        String norm = normalizeDocType(docType);
        switch (norm) {
            case "proposal":
                s.setProposal(false);
                break;
            case "designdocument":
                s.setDesignDocument(false);
                break;
            case "testdocument":
                s.setTestDocument(false);
                break;
            case "thesis":
                s.setThesis(false);
                break;
            default:
                // no-op for unknown types
                break;
        }
    }

    // Get all document deadlines (for students to view)
    @GetMapping("/deadlines")
    @PreAuthorize("hasRole('STUDENT')")
    public List<DocumentTypes> getDeadlines() {
        return documentTypesRepository.findAll();
    }

}
