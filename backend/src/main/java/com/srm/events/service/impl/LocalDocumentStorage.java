package com.srm.events.service.impl;

import com.srm.events.service.interfaces.DocumentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalDocumentStorage implements DocumentStorage {
    private final Path root;

    public LocalDocumentStorage(@Value("${app.storage.root:../storage/documents}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String extension) {
        String key = UUID.randomUUID() + "." + extension;
        Path target = safeTarget(key);
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store uploaded document", exception);
        }
    }

    @Override
    public Path resolve(String storageKey) { return safeTarget(storageKey); }

    @Override
    public void delete(String storageKey) {
        try { Files.deleteIfExists(safeTarget(storageKey)); }
        catch (IOException ignored) { }
    }

    private Path safeTarget(String key) {
        if (key == null || key.isBlank() || key.contains("/") || key.contains("\\") || key.contains("..")) {
            throw new IllegalArgumentException("Invalid document storage key");
        }
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid document storage key");
        return target;
    }
}
