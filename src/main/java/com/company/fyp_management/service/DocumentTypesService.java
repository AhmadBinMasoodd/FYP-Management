package com.company.fyp_management.service;

import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.repository.DocumentTypesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentTypesService {

    private final DocumentTypesRepository documentTypesRepository;

    public DocumentTypesService(DocumentTypesRepository documentTypesRepository) {
        this.documentTypesRepository = documentTypesRepository;
    }

    public DocumentTypes getDocumentTypeById(String id) {
        return documentTypesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DocumentTypes not found with id " + id));
    }

    public List<DocumentTypes> getAllDocumentTypes() {
        // Only return the 4 valid document types
        return documentTypesRepository.findValidDocumentTypes();
    }

    public DocumentTypes createDocumentType(DocumentTypes documentType) {
        return documentTypesRepository.save(documentType);
    }

    public DocumentTypes updateDocumentType(DocumentTypes updatedDocumentType) {
        // Check if exists, if so update; otherwise create
        DocumentTypes existing = documentTypesRepository.findById(updatedDocumentType.getDoc_type()).orElse(null);
        if (existing != null) {
            existing.setDeadline_date(updatedDocumentType.getDeadline_date());
            return documentTypesRepository.save(existing);
        }
        return documentTypesRepository.save(updatedDocumentType);
    }

}
