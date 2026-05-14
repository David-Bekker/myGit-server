package com.myGit.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    /**
     * Store a file on the filesystem
     * @param file The multipart file to store
     * @param repositoryId The repository ID to organize files
     * @return The stored file path
     * @throws IOException if storage fails
     * @throws IllegalArgumentException if file exceeds size limit
     */
    public String storeFile(MultipartFile file, Long repositoryId) throws IOException {
        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        // Create directory structure: uploads/repo-{id}/
        String repoDirName = "repo-" + repositoryId;
        Path repoDirPath = Paths.get(uploadDir, repoDirName);
        
        if (!Files.exists(repoDirPath)) {
            Files.createDirectories(repoDirPath);
        }

        // Generate unique filename to avoid conflicts
        String uniqueFilename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path filePath = repoDirPath.resolve(uniqueFilename);

        // Store the file
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }

    /**
     * Retrieve a file from the filesystem
     * @param filePath The stored file path
     * @return File object
     */
    public File getFile(String filePath) {
        return new File(filePath);
    }

    /**
     * Delete a file from the filesystem
     * @param filePath The stored file path
     * @return true if deletion was successful
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the original filename from stored filename
     * @param storedFilename The stored unique filename
     * @return The original filename
     */
    public String getOriginalFilename(String storedFilename) {
        // Extract original filename from UUID-originalname format
        if (storedFilename.contains("-")) {
            return storedFilename.substring(storedFilename.indexOf("-") + 1);
        }
        return storedFilename;
    }
}
