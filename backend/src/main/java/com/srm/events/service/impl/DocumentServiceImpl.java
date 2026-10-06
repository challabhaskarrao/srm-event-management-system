package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.DocumentResponseDTO;
import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.model.*;
import com.srm.events.service.interfaces.DocumentService;
import com.srm.events.service.interfaces.DocumentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Map<String, String> MIME_BY_EXTENSION = Map.of(
            "pdf", "application/pdf", "png", "image/png", "jpg", "image/jpeg", "jpeg", "image/jpeg");
    private final DocumentDAO documentDAO;
    private final ApplicationDAO applicationDAO;
    private final StudentDAO studentDAO;
    private final FacultyDAO facultyDAO;
    private final EventDAO eventDAO;
    private final StudentAchievementDAO studentAchievementDAO;
    private final DocumentStorage storage;
    private final long maxBytes;

    @Autowired
    public DocumentServiceImpl(DocumentDAO documentDAO, ApplicationDAO applicationDAO, StudentDAO studentDAO,
                               FacultyDAO facultyDAO, EventDAO eventDAO, DocumentStorage storage,
                               StudentAchievementDAO studentAchievementDAO,
                               @Value("${app.storage.max-file-size-bytes:5242880}") long maxBytes) {
        this.documentDAO = documentDAO;
        this.applicationDAO = applicationDAO;
        this.studentDAO = studentDAO;
        this.facultyDAO = facultyDAO;
        this.eventDAO = eventDAO;
        this.studentAchievementDAO = studentAchievementDAO;
        this.storage = storage;
        this.maxBytes = maxBytes;
    }

    public DocumentServiceImpl(DocumentDAO documentDAO, ApplicationDAO applicationDAO, StudentDAO studentDAO,
                               FacultyDAO facultyDAO, EventDAO eventDAO, DocumentStorage storage, long maxBytes) {
        this(documentDAO, applicationDAO, studentDAO, facultyDAO, eventDAO, storage, null, maxBytes);
    }

    @Override
    public List<DocumentTypeDTO> getActiveTypes() { return documentDAO.findActiveTypes(); }

    @Override
    public DocumentResponseDTO upload(Long applicationId, Long typeId, MultipartFile file, Long userId, String role) {
        if (!"STUDENT".equalsIgnoreCase(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        Application application = getApplication(applicationId);
        Student student = studentDAO.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile required"));
        if (!application.getCreatedByStudentId().equals(student.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot upload to another Student's application");
        }
        DocumentTypeDTO type = documentDAO.findActiveTypeById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document type is not active"));
        validatePhase(application, type);
        ValidatedFile validated = validateFile(file);
        String key = storage.store(file, validated.extension());
        try {
            DocumentMetadata document = new DocumentMetadata();
            document.setApplicationId(applicationId);
            document.setDocumentTypeId(typeId);
            document.setUploadedByUserId(userId);
            document.setOriginalFileName(validated.originalName());
            document.setStorageKey(key);
            document.setMimeType(validated.mimeType());
            document.setFileSize(file.getSize());
            return new DocumentResponseDTO(documentDAO.create(document));
        } catch (RuntimeException exception) {
            storage.delete(key);
            throw exception;
        }
    }

    @Override
    public DocumentResponseDTO uploadAchievement(Long achievementId, Long typeId, MultipartFile file, Long userId, String role) {
        if (!"STUDENT".equalsIgnoreCase(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        StudentAchievement achievement = getAchievement(achievementId);
        Student student = studentDAO.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile required"));
        if (!achievement.getStudentId().equals(student.getStudentId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot upload to another Student's achievement");
        if (!Set.of("PENDING_MENTOR_VERIFICATION", "RESUBMISSION_REQUIRED").contains(achievement.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Achievement evidence is locked after Mentor verification");
        }
        DocumentTypeDTO type = documentDAO.findActiveTypeById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document type is not active"));
        if (!"PRE_EVENT".equals(type.getDocumentPhase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Achievement evidence must be a PRE_EVENT document type");
        ValidatedFile validated = validateFile(file);
        String key = storage.store(file, validated.extension());
        try {
            DocumentMetadata document = new DocumentMetadata();
            document.setAchievementId(achievementId);
            document.setDocumentTypeId(typeId);
            document.setUploadedByUserId(userId);
            document.setOriginalFileName(validated.originalName());
            document.setStorageKey(key);
            document.setMimeType(validated.mimeType());
            document.setFileSize(file.getSize());
            return new DocumentResponseDTO(documentDAO.create(document));
        } catch (RuntimeException exception) {
            storage.delete(key);
            throw exception;
        }
    }

    @Override
    public List<DocumentResponseDTO> list(Long applicationId, Long userId, String role) {
        Application application = getApplication(applicationId);
        authorizeRead(application, userId, role);
        return documentDAO.findByApplicationId(applicationId).stream().map(DocumentResponseDTO::new).toList();
    }

    @Override
    public List<DocumentResponseDTO> listAchievement(Long achievementId, Long userId, String role) {
        StudentAchievement achievement = getAchievement(achievementId);
        authorizeRead(achievement, userId, role);
        return documentDAO.findByAchievementId(achievementId).stream().map(DocumentResponseDTO::new).toList();
    }

    @Override
    public DownloadedDocument download(Long documentId, Long userId, String role) {
        DocumentMetadata document = documentDAO.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (document.getApplicationId() != null) authorizeRead(getApplication(document.getApplicationId()), userId, role);
        else if (document.getAchievementId() != null) authorizeRead(getAchievement(document.getAchievementId()), userId, role);
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Document owner is invalid");
        var path = storage.resolve(document.getStorageKey());
        if (!Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stored document not found");
        return new DownloadedDocument(document, path);
    }

    private Application getApplication(Long id) {
        return applicationDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private StudentAchievement getAchievement(Long id) {
        return studentAchievementDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Achievement not found"));
    }

    private void authorizeRead(Application application, Long userId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return;
        if ("STUDENT".equalsIgnoreCase(role)) {
            Student student = studentDAO.findByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile required"));
            if (application.getCreatedByStudentId().equals(student.getStudentId())) return;
        } else if ("MENTOR".equalsIgnoreCase(role)) {
            Faculty faculty = facultyDAO.findActiveByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active faculty profile required"));
            if (applicationDAO.isMentorAssignedToApplication(application.getApplicationId(), faculty.getFacultyId())) return;
        } else if ("HOD".equalsIgnoreCase(role)) {
            Faculty faculty = facultyDAO.findActiveByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active faculty profile required"));
            if (applicationDAO.isApplicationInDepartment(application.getApplicationId(), faculty.getDepartmentId())) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Document access denied");
    }

    private void authorizeRead(StudentAchievement achievement, Long userId, String role) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            Student student = studentDAO.findByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile required"));
            if (achievement.getStudentId().equals(student.getStudentId())) return;
        } else if ("MENTOR".equalsIgnoreCase(role)) {
            Faculty faculty = facultyDAO.findActiveByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active faculty profile required"));
            if (studentAchievementDAO.isMentorAssigned(achievement.getAchievementId(), faculty.getFacultyId())) return;
        } else if ("HOD".equalsIgnoreCase(role)) {
            Faculty faculty = facultyDAO.findActiveByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active faculty profile required"));
            if (studentAchievementDAO.isInDepartment(achievement.getAchievementId(), faculty.getDepartmentId())) return;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative users cannot access Student evidence");
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Document access denied");
    }

    private void validatePhase(Application application, DocumentTypeDTO type) {
        if ("PRE_EVENT".equals(type.getDocumentPhase()) && application.getCurrentStatus() != ApplicationStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pre-event documents are locked after Mentor review");
        }
        if ("POST_EVENT".equals(type.getDocumentPhase())) {
            Event event = eventDAO.findById(application.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
            if (application.getCurrentStatus() != ApplicationStatus.APPROVED || !LocalDate.now().isAfter(event.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Post-event documents require an approved application and a completed event");
            }
        }
    }

    private ValidatedFile validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        if (file.getSize() > maxBytes) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds the 5 MB limit");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("").trim();
        if (original.isEmpty() || original.length() > 255 || original.contains("..") || original.contains("/")
                || original.contains("\\") || original.chars().anyMatch(Character::isISOControl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }
        int dot = original.lastIndexOf('.');
        String extension = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        String expectedMime = MIME_BY_EXTENSION.get(extension);
        if (expectedMime == null || !expectedMime.equalsIgnoreCase(Optional.ofNullable(file.getContentType()).orElse(""))) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only PDF, PNG, and JPEG documents are allowed");
        }
        try {
            byte[] header = Arrays.copyOf(file.getBytes(), (int) Math.min(file.getSize(), 8));
            boolean valid = switch (extension) {
                case "pdf" -> starts(header, new int[]{0x25, 0x50, 0x44, 0x46, 0x2D});
                case "png" -> starts(header, new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
                default -> starts(header, new int[]{0xFF, 0xD8, 0xFF});
            };
            if (!valid) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "File content does not match its declared type");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to inspect uploaded file");
        }
        return new ValidatedFile(original, extension, expectedMime);
    }

    private boolean starts(byte[] bytes, int[] signature) {
        if (bytes.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) if ((bytes[index] & 0xFF) != signature[index]) return false;
        return true;
    }

    private record ValidatedFile(String originalName, String extension, String mimeType) {}
}
