package com.myGit.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myGit.server.model.RepositoryFile;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {
    // Find all files belonging to a specific repository
    List<RepositoryFile> findByRepositoryId(Long repositoryId);
    
    // Find a specific file by repository and filename
    RepositoryFile findByRepositoryIdAndFilename(Long repositoryId, String filename);
}
