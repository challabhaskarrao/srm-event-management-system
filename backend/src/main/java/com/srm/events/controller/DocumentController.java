package com.srm.events.controller;

import com.srm.events.dto.DocumentResponseDTO;
import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.DocumentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RestController
public class DocumentController {
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    public DocumentController(DocumentService documentService, JwtTokenProvider jwtTokenProvider) {
        this.documentService = documentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/api/documents/types")
    public List<DocumentTypeDTO> getTypes(@RequestHeader(value = "Authorization", required = false) String header) {
        token(header);
        return documentService.getActiveTypes();
    }

    @PostMapping(value = "/api/applications/{applicationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> upload(
            @PathVariable Long applicationId,
            @RequestParam Long documentTypeId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(applicationId, documentTypeId, file,
                jwtTokenProvider.getUserIdFromToken(token), jwtTokenProvider.getRoleFromToken(token)));
    }

    @GetMapping("/api/applications/{applicationId}/documents")
    public List<DocumentResponseDTO> list(@PathVariable Long applicationId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return documentService.list(applicationId, jwtTokenProvider.getUserIdFromToken(token),
                jwtTokenProvider.getRoleFromToken(token));
    }

    @PostMapping(value = "/api/achievements/{achievementId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> uploadAchievement(
            @PathVariable Long achievementId,
            @RequestParam Long documentTypeId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadAchievement(achievementId, documentTypeId, file,
                jwtTokenProvider.getUserIdFromToken(token), jwtTokenProvider.getRoleFromToken(token)));
    }

    @GetMapping("/api/achievements/{achievementId}/documents")
    public List<DocumentResponseDTO> listAchievement(@PathVariable Long achievementId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return documentService.listAchievement(achievementId, jwtTokenProvider.getUserIdFromToken(token),
                jwtTokenProvider.getRoleFromToken(token));
    }

    @GetMapping("/api/documents/{documentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId,
            @RequestHeader(value = "Authorization", required = false) String header) throws IOException {
        String token = token(header);
        var download = documentService.download(documentId, jwtTokenProvider.getUserIdFromToken(token),
                jwtTokenProvider.getRoleFromToken(token));
        MediaType mediaType = MediaType.parseMediaType(download.metadata().getMimeType());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.metadata().getOriginalFileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(download.path()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(Files.newInputStream(download.path())));
    }

    private String token(String header) {
        if (header == null || !header.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        String token = header.substring(7).trim();
        if (!jwtTokenProvider.validateToken(token))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        return token;
    }
}
