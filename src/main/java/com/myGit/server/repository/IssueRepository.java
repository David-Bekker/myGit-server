package com.myGit.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myGit.server.model.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    // Find all issues belonging to a specific repository ID
    List<Issue> findByRepositoryId(Long repositoryId);
}
