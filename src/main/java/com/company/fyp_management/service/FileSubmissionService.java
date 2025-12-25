package com.company.fyp_management.service;

import com.company.fyp_management.entity.FileSubmission;
import com.company.fyp_management.repository.FileSubmissionRepository;
import com.company.fyp_management.repository.FeedbackRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.List;

@Service
public class FileSubmissionService {
    private final FileSubmissionRepository fileSubmissionRepository;
    private final FeedbackRepository feedbackRepository;

    // configurable upload dir, default to resources static uploads
    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    public FileSubmissionService(FileSubmissionRepository fileSubmissionRepository, FeedbackRepository feedbackRepository) {
        this.fileSubmissionRepository = fileSubmissionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    // New: save uploaded file to configured uploads dir and persist entity
    @Transactional
    public FileSubmission createFileSubmissionWithUpload(FileSubmission fileSubmission, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is required");
        }

        // resolve uploads directory to an absolute, normalized path
        Path uploadsDirPath = Paths.get(Objects.requireNonNull(uploadDir)).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadsDirPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create uploads directory: " + uploadsDirPath, e);
        }

        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename(), "original filename is null"));
        // basic validation to avoid path traversal
        if (original.contains("..") || original.contains("/") || original.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: " + original);
        }
        // sanitize whitespace
        original = original.replaceAll("\\s+", "_");

        String storedFilename = System.currentTimeMillis() + "_" + original;
        Path target = uploadsDirPath.resolve(storedFilename).normalize();

        // ensure target is inside uploads dir
        if (!target.toAbsolutePath().startsWith(uploadsDirPath)) {
            throw new IllegalArgumentException("Invalid target path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file", e);
        }

        // set saved filename on entity and persist
        fileSubmission.setFilename(storedFilename);
        return fileSubmissionRepository.save(fileSubmission);
    }

    // New: fetch all submissions for a student sorted by submission_datetime desc
    public List<FileSubmission> getSubmissionsByStudentId(Integer studentId) {
        return fileSubmissionRepository.findAllByStudentIdOrderBySubmissionDatetimeDesc(studentId);
    }

    public FileSubmission getFileSubmissionById(Integer id) {
        return fileSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FileSubmission not found with id " + id));
    }

    public FileSubmission updateFileSubmission(FileSubmission updatedFileSubmission) {
        return fileSubmissionRepository.save(updatedFileSubmission);
    }

    // Delete a file submission by ID (used when supervisor rejects/requests revision)
    @Transactional
    public void deleteFileSubmission(Integer id) {
        FileSubmission submission = fileSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FileSubmission not found with id " + id));
        
        // First delete all feedbacks associated with this submission (foreign key constraint)
        feedbackRepository.deleteAllBySubmissionId(id);
        
        // Optionally delete the physical file from disk
        try {
            Path uploadsDirPath = Paths.get(Objects.requireNonNull(uploadDir)).toAbsolutePath().normalize();
            Path filePath = uploadsDirPath.resolve(submission.getFilename()).normalize();
            if (Files.exists(filePath) && filePath.startsWith(uploadsDirPath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log but don't fail - the DB record deletion is more important
            System.err.println("Warning: Could not delete file from disk: " + e.getMessage());
        }
        
        fileSubmissionRepository.deleteById(id);
    }

    // Get all approved submissions (for Evaluation Committee)
    public List<FileSubmission> getAllApprovedSubmissions() {
        return fileSubmissionRepository.findAllApprovedSubmissions();
    }

    // Get approved submissions for a specific student
    public List<FileSubmission> getApprovedSubmissionsByStudentId(Integer studentId) {
        return fileSubmissionRepository.findApprovedSubmissionsByStudentId(studentId);
    }
}
