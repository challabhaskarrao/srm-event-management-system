package com.srm.events.dao.interfaces;

import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.model.DocumentMetadata;
import java.util.List;
import java.util.Optional;

public interface DocumentDAO {
    List<DocumentTypeDTO> findActiveTypes();
    Optional<DocumentTypeDTO> findActiveTypeById(Long documentTypeId);
    DocumentMetadata create(DocumentMetadata document);
    Optional<DocumentMetadata> findById(Long documentId);
    List<DocumentMetadata> findByApplicationId(Long applicationId);
    List<DocumentMetadata> findByAchievementId(Long achievementId);
}
