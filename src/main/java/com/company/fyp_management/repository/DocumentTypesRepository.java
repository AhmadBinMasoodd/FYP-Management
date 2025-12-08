package com.company.fyp_management.repository;

import com.company.fyp_management.entity.DocumentTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentTypesRepository extends JpaRepository<DocumentTypes, String> {
    
    // Get only the 4 valid document types
    @Query("SELECT d FROM DocumentTypes d WHERE d.doc_type IN ('Proposal', 'Design Document', 'Test Document', 'Thesis')")
    List<DocumentTypes> findValidDocumentTypes();
}