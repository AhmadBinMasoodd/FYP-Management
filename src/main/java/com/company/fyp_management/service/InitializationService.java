package com.company.fyp_management.service;

import com.company.fyp_management.entity.Admin;
import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.repository.AdminRepository;
import com.company.fyp_management.repository.DocumentTypesRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class InitializationService implements ApplicationRunner {
    private final AdminRepository adminRepository;
    private final DocumentTypesRepository documentTypesRepository;

    public InitializationService(AdminRepository adminRepository, DocumentTypesRepository documentTypesRepository) {
        this.adminRepository = adminRepository;
        this.documentTypesRepository = documentTypesRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Initialization logic for AdminService
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("password");
            adminRepository.save(admin);
        }

        if (!documentTypesRepository.existsById("Proposal")) {
            DocumentTypes proposal = new DocumentTypes();
            proposal.setDoc_type("Proposal");
            proposal.setDeadline_date(java.time.LocalDate.now().plusMonths(1));
            documentTypesRepository.save(proposal);
        }

        if (!documentTypesRepository.existsById("Design Document")) {
            DocumentTypes proposal = new DocumentTypes();
            proposal.setDoc_type("Design Document");
            proposal.setDeadline_date(java.time.LocalDate.now().plusMonths(1));
            documentTypesRepository.save(proposal);
        }

        if (!documentTypesRepository.existsById("Test Document")) {
            DocumentTypes proposal = new DocumentTypes();
            proposal.setDoc_type("Test Document");
            proposal.setDeadline_date(java.time.LocalDate.now().plusMonths(1));
            documentTypesRepository.save(proposal);
        }

        if (!documentTypesRepository.existsById("Thesis")) {
            DocumentTypes proposal = new DocumentTypes();
            proposal.setDoc_type("Thesis");
            proposal.setDeadline_date(java.time.LocalDate.now().plusMonths(1));
            documentTypesRepository.save(proposal);
        }
    }
}
