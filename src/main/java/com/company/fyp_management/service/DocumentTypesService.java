package com.company.fyp_management.service;

import com.company.fyp_management.entity.DocumentTypes;
import com.company.fyp_management.repository.DocumentTypesRepository;
import org.springframework.stereotype.Service;

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

    public java.util.List<DocumentTypes> getAllDocumentTypes() {
        return documentTypesRepository.findAll();
    }

    public DocumentTypes createDocumentType(DocumentTypes documentType) {
        return documentTypesRepository.save(documentType);
    }

    public DocumentTypes updateDocumentType(DocumentTypes updatedDocumentType) {
        return documentTypesRepository.save(updatedDocumentType);
    }

}
