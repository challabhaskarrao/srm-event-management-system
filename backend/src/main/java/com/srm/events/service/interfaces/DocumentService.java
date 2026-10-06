package com.srm.events.service.interfaces;

import com.srm.events.dto.DocumentResponseDTO;
import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.model.DocumentMetadata;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.List;

public interface DocumentService {
    List<DocumentTypeDTO> getActiveTypes();
    DocumentResponseDTO upload(Long applicationId, Long documentTypeId, MultipartFile file, Long userId, String role);
    List<DocumentResponseDTO> list(Long applicationId, Long userId, String role);
    DocumentResponseDTO uploadAchievement(Long achievementId, Long documentTypeId, MultipartFile file, Long userId, String role);
    List<DocumentResponseDTO> listAchievement(Long achievementId, Long userId, String role);
    DownloadedDocument download(Long documentId, Long userId, String role);

    record DownloadedDocument(DocumentMetadata metadata, Path path) {}
}
