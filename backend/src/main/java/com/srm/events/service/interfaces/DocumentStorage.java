package com.srm.events.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface DocumentStorage {
    String store(MultipartFile file, String extension);
    Path resolve(String storageKey);
    void delete(String storageKey);
}
