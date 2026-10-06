package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.DocumentDAO;
import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.model.DocumentMetadata;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDocumentDAO implements DocumentDAO {
    private final DataSource dataSource;
    public JdbcDocumentDAO(DataSource dataSource) { this.dataSource = dataSource; }

    private DocumentMetadata map(ResultSet rs) throws SQLException {
        DocumentMetadata document = new DocumentMetadata();
        document.setDocumentId(rs.getLong("document_id"));
        long applicationId = rs.getLong("application_id");
        if (!rs.wasNull()) document.setApplicationId(applicationId);
        long achievementId = rs.getLong("achievement_id");
        if (!rs.wasNull()) document.setAchievementId(achievementId);
        document.setDocumentTypeId(rs.getLong("document_type_id"));
        document.setDocumentName(rs.getString("document_name"));
        document.setDocumentPhase(rs.getString("document_phase"));
        document.setUploadedByUserId(rs.getLong("uploaded_by_user_id"));
        document.setOriginalFileName(rs.getString("original_file_name"));
        document.setStorageKey(rs.getString("file_path"));
        document.setMimeType(rs.getString("mime_type"));
        document.setFileSize(rs.getLong("file_size"));
        document.setVerificationStatus(rs.getString("verification_status"));
        document.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        return document;
    }

    @Override
    public List<DocumentTypeDTO> findActiveTypes() {
        String sql = "SELECT document_type_id, document_name, document_phase FROM document_types " +
                "WHERE is_active = TRUE ORDER BY document_phase, document_name";
        List<DocumentTypeDTO> types = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) types.add(new DocumentTypeDTO(rs.getLong(1), rs.getString(2), rs.getString(3)));
            return types;
        } catch (SQLException exception) { throw new RuntimeException("Error loading document types", exception); }
    }

    @Override
    public Optional<DocumentTypeDTO> findActiveTypeById(Long id) {
        String sql = "SELECT document_type_id, document_name, document_phase FROM document_types " +
                "WHERE document_type_id = ? AND is_active = TRUE";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(new DocumentTypeDTO(rs.getLong(1), rs.getString(2), rs.getString(3))) : Optional.empty();
            }
        } catch (SQLException exception) { throw new RuntimeException("Error loading document type", exception); }
    }

    @Override
    public DocumentMetadata create(DocumentMetadata document) {
        String sql = "INSERT INTO documents (application_id, achievement_id, document_type_id, uploaded_by_user_id, " +
                "original_file_name, stored_file_name, file_path, mime_type, file_size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (document.getApplicationId() == null) statement.setNull(1, Types.BIGINT); else statement.setLong(1, document.getApplicationId());
            if (document.getAchievementId() == null) statement.setNull(2, Types.BIGINT); else statement.setLong(2, document.getAchievementId());
            statement.setLong(3, document.getDocumentTypeId());
            statement.setLong(4, document.getUploadedByUserId());
            statement.setString(5, document.getOriginalFileName());
            statement.setString(6, document.getStorageKey());
            statement.setString(7, document.getStorageKey());
            statement.setString(8, document.getMimeType());
            statement.setLong(9, document.getFileSize());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) document.setDocumentId(keys.getLong(1)); }
            return findById(document.getDocumentId()).orElseThrow();
        } catch (SQLException exception) { throw new RuntimeException("Error saving document metadata", exception); }
    }

    private String selectSql(String suffix) {
        return "SELECT d.*, dt.document_name, dt.document_phase FROM documents d " +
                "JOIN document_types dt ON dt.document_type_id = d.document_type_id " + suffix;
    }

    @Override
    public Optional<DocumentMetadata> findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSql("WHERE d.document_id = ?"))) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException exception) { throw new RuntimeException("Error loading document", exception); }
    }

    @Override
    public List<DocumentMetadata> findByApplicationId(Long applicationId) {
        List<DocumentMetadata> documents = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                selectSql("WHERE d.application_id = ? ORDER BY d.uploaded_at DESC"))) {
            statement.setLong(1, applicationId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) documents.add(map(rs)); }
            return documents;
        } catch (SQLException exception) { throw new RuntimeException("Error loading application documents", exception); }
    }

    @Override
    public List<DocumentMetadata> findByAchievementId(Long achievementId) {
        List<DocumentMetadata> documents = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                selectSql("WHERE d.achievement_id = ? ORDER BY d.uploaded_at DESC"))) {
            statement.setLong(1, achievementId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) documents.add(map(rs)); }
            return documents;
        } catch (SQLException exception) { throw new RuntimeException("Error loading achievement documents", exception); }
    }
}
