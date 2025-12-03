package com.company.fyp_management.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.fyp_management.repository.StudentRepository;
import com.company.fyp_management.repository.FacultyRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private FacultyRepository facultyRepository;

	@Autowired
	private PasswordEncoder passwordEncoder; // NEW

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

		// Helper to set session, security context and return response
		java.util.function.BiFunction<Object, String, ResponseEntity<?>> onSuccess = (userObj, userRole) -> {
			// set session attributes (numeric id stored)
			session.setAttribute("userId", numericId);
			session.setAttribute("role", userRole);
			session.setAttribute("user", userObj);

			// set Spring Security Authentication so other secured endpoints can rely on Principal
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase()));
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				numericId, // principal (numeric id)
				null,
				authorities
			);
			SecurityContextHolder.getContext().setAuthentication(auth);

			// Persist security context into HTTP session to survive across requests
			session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

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
					if (stored != null && passwordEncoder.matches(password, stored.toString())) { // use encoder
						return onSuccess.apply(student, "student");
					}
				} catch (NoSuchMethodException e) {
					// fallthrough
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
					if (stored != null && passwordEncoder.matches(password, stored.toString())) { // use encoder
						// determine role from faculty.status (fallback to "faculty")
						String roleFromStatus = "faculty";
						try {
							java.lang.reflect.Method mStatus = faculty.getClass().getMethod("getStatus");
							Object st = mStatus.invoke(faculty);
							if (st != null && !st.toString().isBlank()) {
								roleFromStatus = st.toString();
							}
						} catch (NoSuchMethodException ns) {
							// keep fallback
						} catch (Exception e) {
							// reflection error - keep fallback
						}
						return onSuccess.apply(faculty, roleFromStatus);
					}
				} catch (NoSuchMethodException e) {
					// fallthrough
				} catch (Exception e) {
					// reflection error - treat as invalid credentials
				}
			}
			return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid faculty credentials"));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpSession session) {
		// clear Spring Security context and invalidate session
		SecurityContextHolder.clearContext();
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
