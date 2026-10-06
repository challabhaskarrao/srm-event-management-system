package com.srm.events.dto;

import com.srm.events.model.DocumentMetadata;
import java.time.LocalDateTime;

public class DocumentResponseDTO {
    private Long documentId;
    private Long applicationId;
    private Long documentTypeId;
    private String documentName;
    private String documentPhase;
    private String originalFileName;
    private String mimeType;
    private long fileSize;
    private String verificationStatus;
    private LocalDateTime uploadedAt;

    public DocumentResponseDTO() {}
    public DocumentResponseDTO(DocumentMetadata document) {
        documentId = document.getDocumentId();
        applicationId = document.getApplicationId();
        documentTypeId = document.getDocumentTypeId();
        documentName = document.getDocumentName();
        documentPhase = document.getDocumentPhase();
        originalFileName = document.getOriginalFileName();
        mimeType = document.getMimeType();
        fileSize = document.getFileSize();
        verificationStatus = document.getVerificationStatus();
        uploadedAt = document.getUploadedAt();
    }
    public Long getDocumentId() { return documentId; }
    public Long getApplicationId() { return applicationId; }
    public Long getDocumentTypeId() { return documentTypeId; }
    public String getDocumentName() { return documentName; }
    public String getDocumentPhase() { return documentPhase; }
    public String getOriginalFileName() { return originalFileName; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }
    public String getVerificationStatus() { return verificationStatus; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
