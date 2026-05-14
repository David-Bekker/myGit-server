package com.myGit.server.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.myGit.server.model.GitRepository;
import com.myGit.server.model.RepositoryFile;
import com.myGit.server.repository.GitRepoRepository;
import com.myGit.server.repository.RepositoryFileRepository;
import com.myGit.server.service.FileStorageService;

@RestController
@RequestMapping("/api/repos/{owner}/{repoName}/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    @Autowired
    private RepositoryFileRepository fileRepository;

    @Autowired
    private GitRepoRepository repoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload a file to a repository
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", defaultValue = "anonymous") String uploadedBy) {
        
        try {
            // Find the repository
            Optional<GitRepository> repo = repoRepository.findByNameAndOwner(repoName, owner);
            if (repo.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            GitRepository repository = repo.get();

            // Validate file is not empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
            }

            // Store file on filesystem
            String filePath = fileStorageService.storeFile(file, repository.getId());

            // Create and save file record in database
            RepositoryFile repositoryFile = new RepositoryFile(
                    new File(filePath).getName(),
                    file.getOriginalFilename(),
                    filePath,
                    file.getSize(),
                    file.getContentType(),
                    uploadedBy,
                    repository
            );

            RepositoryFile savedFile = fileRepository.save(repositoryFile);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "fileId", savedFile.getId(),
                    "filename", savedFile.getFilename(),
                    "originalFilename", savedFile.getOriginalFilename(),
                    "fileSize", savedFile.getFileSize(),
                    "uploadedAt", savedFile.getUploadedAt(),
                    "uploadedBy", savedFile.getUploadedBy()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload file: " + e.getMessage()));
        }
    }

    /**
     * List all files in a repository
     */
    @GetMapping
    public ResponseEntity<?> listFiles(@PathVariable String owner, @PathVariable String repoName) {
        Optional<GitRepository> repo = repoRepository.findByNameAndOwner(repoName, owner);
        if (repo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<RepositoryFile> files = fileRepository.findByRepositoryId(repo.get().getId());
        return ResponseEntity.ok(files);
    }

    /**
     * Download a file from a repository
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable String owner,
            @PathVariable String repoName,
            @PathVariable long fileId) {
        
        Optional<RepositoryFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryFile repositoryFile = fileOpt.get();

        // Verify file belongs to the correct repository
        if (!repositoryFile.getRepository().getOwner().equals(owner) || 
            !repositoryFile.getRepository().getName().equals(repoName)) {
            return ResponseEntity.notFound().build();
        }

        File file = fileStorageService.getFile(repositoryFile.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + repositoryFile.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, repositoryFile.getFileType())
                .body(resource);
    }

    /**
     * Get file details
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFileDetails(
            @PathVariable String owner,
            @PathVariable String repoName,
            @PathVariable long fileId) {
        
        Optional<RepositoryFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryFile repositoryFile = fileOpt.get();

        // Verify file belongs to the correct repository
        if (!repositoryFile.getRepository().getOwner().equals(owner) || 
            !repositoryFile.getRepository().getName().equals(repoName)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(repositoryFile);
    }

    /**
     * Delete a file from a repository
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String owner,
            @PathVariable String repoName,
            @PathVariable long fileId) {
        
        Optional<RepositoryFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryFile repositoryFile = fileOpt.get();

        // Verify file belongs to the correct repository
        if (!repositoryFile.getRepository().getOwner().equals(owner) || 
            !repositoryFile.getRepository().getName().equals(repoName)) {
            return ResponseEntity.notFound().build();
        }

        // Delete file from filesystem
        fileStorageService.deleteFile(repositoryFile.getFilePath());

        // Delete file record from database
        fileRepository.deleteById(fileId);

        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }
}
