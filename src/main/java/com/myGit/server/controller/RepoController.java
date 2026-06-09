package com.myGit.server.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myGit.server.model.GitRepository;
import com.myGit.server.model.Issue;
import com.myGit.server.model.PullRequest;
import com.myGit.server.model.User;
import com.myGit.server.repository.GitRepoRepository;
import com.myGit.server.repository.IssueRepository;
import com.myGit.server.repository.PullRequestRepository;
import com.myGit.server.repository.UserRepository;
import com.myGit.server.service.GiteaService;

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

    @Autowired
    private GiteaService giteaService;

    @GetMapping
    public List<GitRepository> getAllRepos() {
        return repoRepository.findAll();
    }

    @PostMapping
    public GitRepository createRepo(@RequestBody GitRepository repo) {
        GitRepository saved = repoRepository.save(repo);

        try {
            giteaService.createRepository(saved.getOwner(), saved.getName(), saved.getDescription());
            saved.setCloneUrl(giteaService.getHttpCloneUrl(saved.getOwner(), saved.getName()));
        } catch (Exception ex) {
            // If the Gitea call fails, we still keep the local repository record.
            // This keeps app behavior stable and allows retry or manual recovery.
            ex.printStackTrace();
        }

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


    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRepo(@PathVariable Long id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<GitRepository> repoOpt = repoRepository.findById(id);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GitRepository repo = repoOpt.get();

        if (!repo.getOwner().equals(currentUser.getUsername())) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Forbidden: You can only delete your own repositories."));
        }

        repoRepository.delete(repo);
        return ResponseEntity.ok(java.util.Map.of("message", "Repository deleted successfully."));
    }

    @PutMapping("/{owner}/{repoName}/issues/{issueId}/close")
    public ResponseEntity<?> closeIssue(
            @PathVariable String owner, 
            @PathVariable String repoName, 
            @PathVariable Long issueId,
            Authentication authentication) {
        
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"development".equals(currentUser.getDepartment())) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Forbidden: Only users in the 'development' department can edit issues."));
        }

        Optional<Issue> issueOpt = issueRepository.findById(issueId);
        if (issueOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Issue issue = issueOpt.get();
        issue.setStatus("closed");
        issueRepository.save(issue);

        return ResponseEntity.ok(issue);
    }

}