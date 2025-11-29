package com.company.fyp_management.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.fyp_management.repository.StudentRepository;
import com.company.fyp_management.repository.FacultyRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    // Simple DTO for login request
    static class LoginRequest {
        private String id;       // expects strings like "STU-123" or "FAC-45"
        private String password;
        private String role; // ignored: role is determined from id prefix

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        String rawId = req.getId();
        String password = req.getPassword();

        if (rawId == null || password == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "id and password are required"));
        }

        String up = rawId.toUpperCase();
        if (!(up.startsWith("STU-") || up.startsWith("FAC-"))) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "id must start with 'STU-' or 'FAC-'"));
        }

        // remove only the first 4 characters (e.g., "STU-" or "FAC-")
        String numericPart = rawId.length() > 4 ? rawId.substring(4) : "";
        Integer numericId;
        try {
            numericId = Integer.parseInt(numericPart);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "invalid numeric id part"));
        }

        // Helper to set session and return response
        java.util.function.BiFunction<Object, String, ResponseEntity<?>> onSuccess = (userObj, userRole) -> {
            session.setAttribute("userId", numericId);
            session.setAttribute("role", userRole);
            session.setAttribute("user", userObj);
            return ResponseEntity.ok(java.util.Map.of("message", "Login successful", "role", userRole));
        };

        boolean isStudent = up.startsWith("STU-");
        if (isStudent) {
            java.util.Optional<?> studentOpt = studentRepository.findById(numericId);
            if (studentOpt.isPresent()) {
                Object student = studentOpt.get();
                try {
                    java.lang.reflect.Method m = student.getClass().getMethod("getPassword");
                    Object stored = m.invoke(student);
                    if (stored != null && password.equals(stored.toString())) {
                        return onSuccess.apply(student, "student");
                    }
                } catch (NoSuchMethodException e) {
                    // fallthrough to invalid credentials
                } catch (Exception e) {
                    // reflection error - treat as invalid credentials
                }
            }
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid student credentials"));
        } else { // faculty
            java.util.Optional<?> facultyOpt = facultyRepository.findById(numericId);
            if (facultyOpt.isPresent()) {
                Object faculty = facultyOpt.get();
                try {
                    java.lang.reflect.Method m = faculty.getClass().getMethod("getPassword");
                    Object stored = m.invoke(faculty);
                    if (stored != null && password.equals(stored.toString())) {
                        return onSuccess.apply(faculty, "faculty");
                    }
                } catch (NoSuchMethodException e) {
                    // fallthrough to invalid credentials
                } catch (Exception e) {
                    // reflection error - treat as invalid credentials
                }
            }
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid faculty credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");
        if (userId == null || role == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Not authenticated"));
        }
        // Return minimal session info. The "user" object is returned as-is (may include full entity).
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Authenticated",
            "userId", userId,
            "role", role,
            "user", user
        ));
    }

}
