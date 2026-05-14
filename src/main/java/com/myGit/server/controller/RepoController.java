package com.myGit.server.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myGit.server.model.GitRepository;
import com.myGit.server.model.Issue;
import com.myGit.server.model.PullRequest;
import com.myGit.server.repository.GitRepoRepository;
import com.myGit.server.repository.IssueRepository;
import com.myGit.server.repository.PullRequestRepository;

@RestController
@RequestMapping("/api/repos")
@CrossOrigin(origins = "http://localhost:3000")
public class RepoController {

    @Autowired
    private GitRepoRepository repoRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private PullRequestRepository pullRequestRepository;

    @GetMapping
    public List<GitRepository> getAllRepos() {
        return repoRepository.findAll();
    }

    @PostMapping
    public GitRepository createRepo(@RequestBody GitRepository repo) {
        @SuppressWarnings("null")
        GitRepository saved = repoRepository.save(repo);
        return saved;
    }

    @GetMapping("/owner/{username}")
    public List<GitRepository> getReposByOwner(@PathVariable String username) {
        return repoRepository.findByOwner(username);
    }

    // --- NEW DYNAMIC ENDPOINTS FOR ISSUES & PRs ---

    @GetMapping("/{owner}/{repoName}/issues")
    public ResponseEntity<List<Issue>> getIssues(@PathVariable String owner, @PathVariable String repoName) {
        // 1. Find the repository first
        Optional<GitRepository> repo = repoRepository.findByNameAndOwner(repoName, owner);
        
        // 2. Return issues belonging to that repo ID
        return repo.map(value -> ResponseEntity.ok(issueRepository.findByRepositoryId(value.getId())))
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{owner}/{repoName}/pulls")
    public ResponseEntity<List<PullRequest>> getPulls(@PathVariable String owner, @PathVariable String repoName) {
        // 1. Find the repository first
        Optional<GitRepository> repo = repoRepository.findByNameAndOwner(repoName, owner);
        
        // 2. Return PRs belonging to that repo ID
        return repo.map(value -> ResponseEntity.ok(pullRequestRepository.findByRepositoryId(value.getId())))
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
}