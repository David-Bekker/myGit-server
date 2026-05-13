package com.myGit.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myGit.server.model.GitRepository;

public interface GitRepoRepository extends JpaRepository<GitRepository, Long> {
    // This allows the frontend to find repos by owner and name
    Optional<GitRepository> findByOwnerAndName(String owner, String name);
}