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
import java.util.Optional;
import java.util.List;
import java.lang.reflect.Field;

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

        // Return all submissions for this student, sorted by date
        List<FileSubmission> submissions = fileSubmissionRepository.findAllByStudentIdOrderBySubmissionDatetimeDesc(studentId);
        return submissions;
    }

    @GetMapping("/grades/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public Grades getGradesByStudentId(@PathVariable("studentId") Integer studentId) {
        return gradesService.getGradesByStudentId(studentId);
    }

    @PostMapping(value = "/download", consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    public void downloadFile(@RequestBody FileSubmission fileSubmission, HttpServletResponse response) throws IOException {
        // validate body
        if (fileSubmission == null) {
            response.setStatus(400);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"request body (file_submission) is required\"}");
            return;
        }

        // try to extract an integer id from the FileSubmission via reflection (handles different field names)
        Integer fileId = null;
        try {
            Field[] fields = fileSubmission.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                String name = f.getName().toLowerCase();
                // common id field name candidates
                if (name.equals("id") || name.equals("fileid") || name.equals("file_id") || name.endsWith("id")) {
                    Object val = f.get(fileSubmission);
                    if (val == null) continue;
                    if (val instanceof Number) {
                        fileId = ((Number) val).intValue();
                    } else {
                        try {
                            fileId = Integer.parseInt(String.valueOf(val));
                        } catch (NumberFormatException ignored) { }
                    }
                    if (fileId != null) break;
                }
            }
        } catch (IllegalAccessException ignored) {
            // fall through - will validate fileId below
        }

        if (fileId == null) {
            response.setStatus(400);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"file id not found in request body\"}");
            return;
        }

        // load persistent object
        Optional<FileSubmission> fsOpt = fileSubmissionRepository.findById(fileId);
        if (fsOpt.isEmpty()) {
            response.setStatus(404);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"FileSubmission not found for id: " + fileId + "\"}");
            return;
        }
        FileSubmission persisted = fsOpt.get();

        // get filename from persistent object and stream it
        String filename = persisted.getFilename();
        if (filename == null || filename.isBlank()) {
            response.setStatus(404);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"No stored filename for FileSubmission id: " + fileId + "\"}");
            return;
        }

        Path uploadsDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = uploadsDirPath.resolve(filename).normalize();

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
