package com.myGit.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myGit.server.model.PullRequest;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    List<PullRequest> findByRepositoryId(Long repositoryId);
}
