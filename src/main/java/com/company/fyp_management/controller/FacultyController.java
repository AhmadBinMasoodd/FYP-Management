package com.company.fyp_management.controller;
import com.company.fyp_management.entity.Faculty;
import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.entity.Student;
import com.company.fyp_management.entity.Grades;
import com.company.fyp_management.entity.State;
import com.company.fyp_management.entity.Feedback;
import com.company.fyp_management.service.StateService;
import com.company.fyp_management.service.GradesService;
import com.company.fyp_management.service.FacultyService;
import com.company.fyp_management.service.StudentService;
import com.company.fyp_management.service.FileSubmissionService;
import com.company.fyp_management.service.DocumentTypesService;
import com.company.fyp_management.service.FeedbackService;
import com.company.fyp_management.service.NotificationService;
import com.company.fyp_management.repository.FeedbackRepository;
import com.company.fyp_management.repository.GradesRepository;
import com.company.fyp_management.repository.FileSubmissionRepository;
import com.company.fyp_management.repository.DocumentTypesRepository;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;
    private final StudentService studentService;
    private final FileSubmissionService fileSubmissionService;
    private final GradesService gradesService;
    private final DocumentTypesService documentTypesService;
    private final StateService stateService;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final GradesRepository gradesRepository;
    private final FileSubmissionRepository fileSubmissionRepository;
    private final NotificationService notificationService;
    private final DocumentTypesRepository documentTypesRepository;

    public FacultyController(FacultyService facultyService, StudentService studentService, FileSubmissionService fileSubmissionService, GradesService gradesService, DocumentTypesService documentTypesService, StateService stateService, FeedbackService feedbackService, FeedbackRepository feedbackRepository, GradesRepository gradesRepository, FileSubmissionRepository fileSubmissionRepository, NotificationService notificationService, DocumentTypesRepository documentTypesRepository) {
        this.facultyService = facultyService;
        this.studentService = studentService;
        this.fileSubmissionService = fileSubmissionService;
        this.gradesService = gradesService;
        this.documentTypesService = documentTypesService;
        this.stateService = stateService;
        this.feedbackService = feedbackService;
        this.feedbackRepository = feedbackRepository;
        this.gradesRepository = gradesRepository;
        this.fileSubmissionRepository = fileSubmissionRepository;
        this.notificationService = notificationService;
        this.documentTypesRepository = documentTypesRepository;
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

    @GetMapping("/allstudents")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER') or hasRole('FYP COMMITTEE MEMBER')")
    public java.util.List<Student> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
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
        
        // Send notification to student
        DocumentTypes docType = persisted.getdocumentType();
        String docTypeName = docType != null ? docType.getDoc_type() : "document";
        notificationService.notifyStudentApproval(studentId, docTypeName);
        
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
        
        // Send notification to student before deleting submission
        notificationService.notifyStudentRejection(studentId, doc_type);
        
        // Delete the rejected submission so it no longer appears
        fileSubmissionService.deleteFileSubmission(fsId);
        
        return updated;
    }

    public boolean can_grade(Integer studentId) {
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        Student student = studentOpt.get();
        Integer supervisorId = student.getSupervisorId();
        if (supervisorId == null || supervisorId.intValue() == -1) {
            return false;
        }
        if (student.getProposal() == true || student.getDesignDocument() == true || student.getTestDocument() == true || student.getThesis() == true) {
            return false;
        }
        return true;
    }

    @PostMapping("/gradestudent")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public Grades gradeStudent(@RequestBody Grades grades) {
        Student student = grades.getStudent();
        Integer studentId = student.getNumericId();
        if (!can_grade(studentId)) {
            throw new IllegalStateException("Student is not eligible for grading");
        }
        Grades createdGrades = gradesService.createGrades(grades);
        return createdGrades;
    }

    @GetMapping("/grades/{studentId}")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public Grades getGradesByStudentId(@PathVariable("studentId") Integer studentId) {
        return gradesService.getGradesByStudentId(studentId);
    }

    @GetMapping("/approvedsubmissions")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public List<FileSubmission> getApprovedSubmissions() {
        return fileSubmissionService.getAllApprovedSubmissions();
    }

    @PostMapping("/evalrequestrevision")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public Student evalRequestRevision(@RequestBody java.util.Map<String, Object> request) {
        Integer studentId = null;
        Object studentIdObj = request.get("student_id");
        if (studentIdObj instanceof Number) {
            studentId = ((Number) studentIdObj).intValue();
        } else if (studentIdObj != null) {
            studentId = Integer.parseInt(studentIdObj.toString());
        }
        
        String docType = (String) request.get("doc_type");
        
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required");
        }
        if (docType == null || docType.trim().isEmpty()) {
            throw new IllegalArgumentException("Document type is required");
        }

        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        Student student = studentOpt.get();

        // Reset the appropriate document flag to allow resubmission
        if (docType.equals("Proposal")) {
            student.setProposal(true);
        } else if (docType.equals("Design Document")) {
            student.setDesignDocument(true);
        } else if (docType.equals("Test Document")) {
            student.setTestDocument(true);
        } else if (docType.equals("Thesis")) {
            student.setThesis(true);
        } else {
            throw new IllegalStateException("Unknown document type: " + docType);
        }
        
        Student updated = studentService.updateStudent(student);
        
        // Send notification to student
        notificationService.createNotification(
            studentId, 
            "student",
            "Revision Requested by Evaluation Committee",
            "The Evaluation Committee has requested a revision for your " + docType + " due to grade F. Please review and resubmit.",
            "eval_revision",
            null
        );
        
        return updated;
    }

    @PostMapping("/changedeadline")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public DocumentTypes changeDeadline(@RequestBody DocumentTypes document_types) {
        return documentTypesService.updateDocumentType(document_types);
    }

    @GetMapping("/documenttypes")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public List<DocumentTypes> getAllDocumentTypes() {
        return documentTypesService.getAllDocumentTypes();
    }

    @GetMapping("/releasegrades")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public State releaseGrades() {
        State state = new State();
        state.setstateName("release_grades");
        state.setValue(true);
        return stateService.updateState(state);
    }

    @GetMapping("/hidegrades")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public State hideGrades() {
        State state = new State();
        state.setstateName("release_grades");
        state.setValue(false);
        return stateService.updateState(state);
    }

    @GetMapping("/allstudentgrades")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public List<Map<String, Object>> getAllStudentGrades() {
        List<Grades> allGrades = gradesRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Grades grade : allGrades) {
            try {
                Map<String, Object> studentGradeInfo = new HashMap<>();
                Student student = grade.getStudent();
                
                if (student == null) continue;
                
                studentGradeInfo.put("studentId", student.getNumericId());
                studentGradeInfo.put("studentName", student.getName());
                studentGradeInfo.put("studentEmail", student.getEmail());
                studentGradeInfo.put("grade", String.valueOf(grade.getGrade()));
                studentGradeInfo.put("rubric1", grade.getRubric1());
                studentGradeInfo.put("rubric2", grade.getRubric2());
                studentGradeInfo.put("rubric3", grade.getRubric3());
                studentGradeInfo.put("rubric4", grade.getRubric4());
                studentGradeInfo.put("rubric5", grade.getRubric5());
                studentGradeInfo.put("rubric6", grade.getRubric6());
                
                // Get supervisor info
                Integer supervisorId = student.getSupervisorId();
                if (supervisorId != null && supervisorId > 0) {
                    Optional<Faculty> supervisor = facultyService.getFacultyById(supervisorId);
                    if (supervisor.isPresent()) {
                        studentGradeInfo.put("supervisorId", supervisorId);
                        studentGradeInfo.put("supervisorName", supervisor.get().getName());
                    } else {
                        studentGradeInfo.put("supervisorId", null);
                        studentGradeInfo.put("supervisorName", "Not Assigned");
                    }
                } else {
                    studentGradeInfo.put("supervisorId", null);
                    studentGradeInfo.put("supervisorName", "Not Assigned");
                }
                
                result.add(studentGradeInfo);
            } catch (Exception e) {
                // Skip this grade if there's an error
                continue;
            }
        }
        
        return result;
    }

    @GetMapping("/gradereleasestatus")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public Map<String, Object> getGradeReleaseStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            State state = stateService.getStateByName("release_grades");
            result.put("released", state != null && state.getValue());
        } catch (Exception e) {
            result.put("released", false);
        }
        return result;
    }

    @PostMapping("/addfeedback")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Feedback addFeedback(@RequestBody java.util.Map<String, Object> request, HttpSession session) {
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

        Integer fileId = null;
        Object fileIdObj = request.get("file_id");
        if (fileIdObj instanceof Number) {
            fileId = ((Number) fileIdObj).intValue();
        } else if (fileIdObj != null) {
            fileId = Integer.parseInt(fileIdObj.toString());
        }
        
        String content = (String) request.get("content");
        
        if (fileId == null) {
            throw new IllegalArgumentException("File submission id is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback content is required");
        }

        FileSubmission submission = fileSubmissionService.getFileSubmissionById(fileId);
        if (submission == null) {
            throw new IllegalArgumentException("File submission not found");
        }

        Student student = submission.getStudent();
        if (student == null) {
            throw new IllegalStateException("File submission has no associated student");
        }

        Integer supervisorId = student.getSupervisorId();
        if (supervisorId == null || supervisorId.intValue() != userId.intValue()) {
            throw new SecurityException("Not authorized to add feedback to this submission");
        }

        Feedback feedback = new Feedback();
        feedback.setSubmission(submission);
        feedback.setContent(content.trim());
        
        Feedback savedFeedback = feedbackRepository.save(feedback);
        
        // Send notification to student
        DocumentTypes docType = submission.getdocumentType();
        String docTypeName = docType != null ? docType.getDoc_type() : "document";
        notificationService.notifyStudentFeedback(student.getNumericId(), docTypeName, content.trim());
        
        return savedFeedback;
    }

    // FYP Committee Stats endpoint
    @GetMapping("/fypcommittee/stats")
    @PreAuthorize("hasRole('FYP COMMITTEE MEMBER')")
    public java.util.Map<String, Object> getFypCommitteeStats() {
        // Total students
        long totalStudents = studentService.getAllStudents().size();
        
        // Completed projects (students who have grades)
        long completedProjects = gradesRepository.count();
        
        // Pending reviews (submissions that are not approved yet)
        List<FileSubmission> allSubmissions = fileSubmissionRepository.findAll();
        long pendingReviews = allSubmissions.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIs_approved()))
                .count();
        
        return java.util.Map.of(
            "totalStudents", totalStudents,
            "completedProjects", completedProjects,
            "pendingReviews", pendingReviews
        );
    }

    // Check deadlines and auto-assign F grades for students with pending submissions
    @PostMapping("/checkdeadlines")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public Map<String, Object> checkDeadlinesAndAssignFGrades() {
        List<Map<String, Object>> affectedStudents = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Get all document types with deadlines
        List<DocumentTypes> documentTypes = documentTypesRepository.findValidDocumentTypes();
        
        // Check each document type's deadline
        for (DocumentTypes docType : documentTypes) {
            LocalDate deadline = docType.getDeadline_date();
            if (deadline == null || !deadline.isBefore(today)) {
                // Deadline not set or not passed yet
                continue;
            }
            
            String docTypeName = docType.getDoc_type();
            
            // Get all students
            List<Student> allStudents = studentService.getAllStudents();
            
            for (Student student : allStudents) {
                Integer studentId = student.getNumericId();
                
                // Check if student already has a grade
                Optional<Grades> existingGrade = gradesRepository.findByStudent(student);
                if (existingGrade.isPresent()) {
                    continue; // Already graded
                }
                
                // Check if student has submitted and got approval for this document type
                boolean hasApprovedSubmission = false;
                List<FileSubmission> studentSubmissions = fileSubmissionRepository.findByStudent(student);
                
                for (FileSubmission submission : studentSubmissions) {
                    DocumentTypes subDocType = submission.getdocumentType();
                    if (subDocType != null && subDocType.getDoc_type().equals(docTypeName)) {
                        if (Boolean.TRUE.equals(submission.getIs_approved())) {
                            hasApprovedSubmission = true;
                            break;
                        }
                    }
                }
                
                // If no approved submission and deadline passed, assign F grade
                if (!hasApprovedSubmission) {
                    // Create F grade with all rubrics set to 1 (lowest)
                    Grades fGrade = new Grades();
                    fGrade.setStudent(student);
                    fGrade.setRubric1(1);
                    fGrade.setRubric2(1);
                    fGrade.setRubric3(1);
                    fGrade.setRubric4(1);
                    fGrade.setRubric5(1);
                    fGrade.setRubric6(1);
                    fGrade.setGrade('F');
                    
                    gradesRepository.save(fGrade);
                    
                    // Send notification to student
                    notificationService.createNotification(
                        studentId,
                        "student",
                        "Grade Assigned: F",
                        "You have received grade F for " + docTypeName + " because the deadline has passed without an approved submission.",
                        "grade_f",
                        null
                    );
                    
                    Map<String, Object> affectedStudent = new HashMap<>();
                    affectedStudent.put("studentId", studentId);
                    affectedStudent.put("studentName", student.getName());
                    affectedStudent.put("documentType", docTypeName);
                    affectedStudent.put("deadline", deadline.toString());
                    affectedStudents.add(affectedStudent);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", affectedStudents.isEmpty() ? "No students affected" : "F grades assigned");
        result.put("affectedCount", affectedStudents.size());
        result.put("affectedStudents", affectedStudents);
        return result;
    }

    // Get students with pending submissions (not approved) for passed deadlines
    @GetMapping("/pendingdeadlines")
    @PreAuthorize("hasRole('EVALUATION COMMITTEE MEMBER')")
    public List<Map<String, Object>> getStudentsWithPendingDeadlines() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Get all document types with deadlines
        List<DocumentTypes> documentTypes = documentTypesRepository.findValidDocumentTypes();
        
        for (DocumentTypes docType : documentTypes) {
            LocalDate deadline = docType.getDeadline_date();
            if (deadline == null) continue;
            
            boolean isPassed = deadline.isBefore(today);
            String docTypeName = docType.getDoc_type();
            
            // Get all students
            List<Student> allStudents = studentService.getAllStudents();
            
            for (Student student : allStudents) {
                Integer studentId = student.getNumericId();
                
                // Check if student already has a grade
                Optional<Grades> existingGrade = gradesRepository.findByStudent(student);
                if (existingGrade.isPresent()) {
                    continue; // Already graded
                }
                
                // Check submission status for this document type
                boolean hasSubmission = false;
                boolean isApproved = false;
                List<FileSubmission> studentSubmissions = fileSubmissionRepository.findByStudent(student);
                
                for (FileSubmission submission : studentSubmissions) {
                    DocumentTypes subDocType = submission.getdocumentType();
                    if (subDocType != null && subDocType.getDoc_type().equals(docTypeName)) {
                        hasSubmission = true;
                        if (Boolean.TRUE.equals(submission.getIs_approved())) {
                            isApproved = true;
                            break;
                        }
                    }
                }
                
                // Add to result if deadline passed and no approved submission
                if (isPassed && !isApproved) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("studentId", studentId);
                    entry.put("studentName", student.getName());
                    entry.put("email", student.getEmail());
                    entry.put("documentType", docTypeName);
                    entry.put("deadline", deadline.toString());
                    entry.put("hasSubmission", hasSubmission);
                    entry.put("isApproved", isApproved);
                    entry.put("deadlinePassed", true);
                    result.add(entry);
                }
            }
        }
        
        return result;
    }

    // Admin Stats endpoint - for Dashboard5
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Map<String, Object> getAdminStats() {
        // Total students
        long totalStudents = studentService.getAllStudents().size();
        
        // Total users (just students for now, can be expanded)
        long totalUsers = totalStudents;
        
        // Active projects (students with supervisors assigned, i.e., supervisorId != -1)
        long activeProjects = studentService.getAllStudents().stream()
                .filter(s -> s.getSupervisorId() != null && s.getSupervisorId() != -1)
                .count();
        
        // Total submissions
        long totalSubmissions = fileSubmissionRepository.count();
        
        return java.util.Map.of(
            "totalUsers", totalUsers,
            "totalStudents", totalStudents,
            "activeProjects", activeProjects,
            "totalSubmissions", totalSubmissions
        );
    }

    // Admin endpoint to get all student reports with grades
    @GetMapping("/admin/studentreports")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAdminStudentReports() {
        List<Student> allStudents = studentService.getAllStudents();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Student student : allStudents) {
            try {
                Map<String, Object> studentInfo = new HashMap<>();
                
                studentInfo.put("studentId", student.getNumericId());
                studentInfo.put("studentName", student.getName());
                studentInfo.put("email", student.getEmail());
                studentInfo.put("address", student.getAddress());
                
                // Get supervisor info
                Integer supervisorId = student.getSupervisorId();
                if (supervisorId != null && supervisorId > 0) {
                    Optional<Faculty> supervisor = facultyService.getFacultyById(supervisorId);
                    if (supervisor.isPresent()) {
                        studentInfo.put("supervisorId", supervisorId);
                        studentInfo.put("supervisorName", supervisor.get().getName());
                    } else {
                        studentInfo.put("supervisorId", null);
                        studentInfo.put("supervisorName", "Not Assigned");
                    }
                } else {
                    studentInfo.put("supervisorId", null);
                    studentInfo.put("supervisorName", "Not Assigned");
                }
                
                // Get grades if exists
                Optional<Grades> gradesOpt = gradesRepository.findByStudent(student);
                if (gradesOpt.isPresent()) {
                    Grades grade = gradesOpt.get();
                    studentInfo.put("grade", String.valueOf(grade.getGrade()));
                    studentInfo.put("rubric1", grade.getRubric1());
                    studentInfo.put("rubric2", grade.getRubric2());
                    studentInfo.put("rubric3", grade.getRubric3());
                    studentInfo.put("rubric4", grade.getRubric4());
                    studentInfo.put("rubric5", grade.getRubric5());
                    studentInfo.put("rubric6", grade.getRubric6());
                    int totalScore = grade.getRubric1() + grade.getRubric2() + grade.getRubric3() + 
                                     grade.getRubric4() + grade.getRubric5() + grade.getRubric6();
                    studentInfo.put("totalScore", totalScore);
                    studentInfo.put("isGraded", true);
                } else {
                    studentInfo.put("grade", null);
                    studentInfo.put("rubric1", null);
                    studentInfo.put("rubric2", null);
                    studentInfo.put("rubric3", null);
                    studentInfo.put("rubric4", null);
                    studentInfo.put("rubric5", null);
                    studentInfo.put("rubric6", null);
                    studentInfo.put("totalScore", null);
                    studentInfo.put("isGraded", false);
                }
                
                // Count submissions
                List<FileSubmission> submissions = fileSubmissionRepository.findAllByStudentIdOrderBySubmissionDatetimeDesc(student.getNumericId());
                studentInfo.put("submissionCount", submissions.size());
                
                result.add(studentInfo);
            } catch (Exception e) {
                // Skip this student if there's an error
                continue;
            }
        }
        
        return result;
    }

    // Admin endpoint to get all faculty members
    @GetMapping("/allfaculty")
    public List<Faculty> getAllFaculty() {
        return facultyService.getAllFaculty();
    }
}
