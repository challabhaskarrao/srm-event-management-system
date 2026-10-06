package com.srm.events.dto;

public class DocumentTypeDTO {
    private Long documentTypeId;
    private String documentName;
    private String documentPhase;

    public DocumentTypeDTO() {}
    public DocumentTypeDTO(Long documentTypeId, String documentName, String documentPhase) {
        this.documentTypeId = documentTypeId;
        this.documentName = documentName;
        this.documentPhase = documentPhase;
    }
    public Long getDocumentTypeId() { return documentTypeId; }
    public void setDocumentTypeId(Long documentTypeId) { this.documentTypeId = documentTypeId; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentPhase() { return documentPhase; }
    public void setDocumentPhase(String documentPhase) { this.documentPhase = documentPhase; }
}
