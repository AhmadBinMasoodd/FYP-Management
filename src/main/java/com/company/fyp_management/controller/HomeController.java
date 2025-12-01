package com.company.fyp_management.controller;

import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.entity.Student;
import com.company.fyp_management.entity.Grades;
import com.company.fyp_management.repository.FileSubmissionRepository;
import com.company.fyp_management.service.DocumentTypesService;
import com.company.fyp_management.service.StudentService;
import com.company.fyp_management.service.GradesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class HomeController {

    // configurable upload dir (same default used by service)
    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    private final FileSubmissionRepository fileSubmissionRepository;
    private final DocumentTypesService documentTypesService;
    private final StudentService studentService;
    private final GradesService gradesService;

    public HomeController(FileSubmissionRepository fileSubmissionRepository, DocumentTypesService documentTypesService, StudentService studentService, GradesService gradesService) {
        this.fileSubmissionRepository = fileSubmissionRepository;
        this.documentTypesService = documentTypesService;
        this.studentService = studentService;
        this.gradesService = gradesService;
    }

    @GetMapping(value = "/documenttypes")
    public List<DocumentTypes> getDocumentTypes() {
        List<DocumentTypes> docTypes = documentTypesService.getAllDocumentTypes();
        return docTypes;
    }

    @GetMapping("/submissions/{id}")
    @PreAuthorize("isAuthenticated()")
    public List<FileSubmission> getLatestSubmissionsByStudentId(@PathVariable("id") Integer studentId) {
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        Student student = studentOpt.get();

        boolean isProposalSubmitted = student.getProposal();
        boolean isDesignDocSubmitted = student.getDesignDocument();
        boolean isTestDocSubmitted = student.getTestDocument();
        boolean isThesisSubmitted = student.getThesis();

        List<FileSubmission> latestSubmissions = new java.util.ArrayList<>();
        if (!isProposalSubmitted)
            latestSubmissions.add(fileSubmissionRepository.findLatestSubmission(studentId, "Proposal"));
        if (!isDesignDocSubmitted)
            latestSubmissions.add(fileSubmissionRepository.findLatestSubmission(studentId, "Design Document"));
        if (!isTestDocSubmitted)
            latestSubmissions.add(fileSubmissionRepository.findLatestSubmission(studentId, "Test Document"));
        if (!isThesisSubmitted)
            latestSubmissions.add(fileSubmissionRepository.findLatestSubmission(studentId, "Thesis"));

        return latestSubmissions;
    }

    @GetMapping("/grades/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public Grades getGradesByStudentId(@PathVariable("studentId") Integer studentId) {
        return gradesService.getGradesByStudentId(studentId);
    }

    @PostMapping(value = "/download", consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    public void downloadFile(@RequestBody Map<String, Object> req, HttpServletResponse response) throws IOException {
        // try to resolve filename directly if provided
        String filename = null;
        if (req != null) {
            Object fn = req.get("filename");
            if (fn != null) filename = fn.toString();
        }

        // if filename not provided, try to resolve by file_id
        if (true) {
            if (req == null || (req.get("file_id") == null && req.get("fileId") == null && req.get("id") == null)) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"file_id or filename is required in request body\"}");
                return;
            }

            Integer fileId = null;
            try {
                Object idObj = req.get("file_id");
                if (idObj == null) idObj = req.get("fileId");
                if (idObj == null) idObj = req.get("id");
                if (idObj != null) {
                    fileId = Integer.parseInt(String.valueOf(idObj));
                }
            } catch (NumberFormatException e) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"file_id must be a number\"}");
                return;
            }

            if (fileId == null) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"file_id is required\"}");
                return;
            }

            Optional<FileSubmission> fsOpt = fileSubmissionRepository.findById(fileId);
            if (fsOpt.isEmpty()) {
                response.setStatus(404);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"FileSubmission not found for id: " + fileId + "\"}");
                return;
            }
            FileSubmission submission = fsOpt.get();
            filename = submission.getFilename();
            if (filename == null || filename.isBlank()) {
                response.setStatus(404);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"No stored filename for FileSubmission id: " + fileId + "\"}");
                return;
            }
        }

        Path uploadsDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = uploadsDirPath.resolve(filename).normalize();

        // basic validation: ensure target is inside uploadsDir and is a regular existing file
        if (!target.startsWith(uploadsDirPath) || !Files.exists(target) || !Files.isRegularFile(target)) {
            response.setStatus(404);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"File not found\"}");
            return;
        }

        String contentType = Files.probeContentType(target);
        if (contentType == null) contentType = "application/octet-stream";

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + target.getFileName().toString() + "\"");
        Files.copy(target, response.getOutputStream());
        response.getOutputStream().flush();
    }
}
