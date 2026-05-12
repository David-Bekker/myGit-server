package com.myGit.server.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myGit.server.model.GitRepository;
import com.myGit.server.repository.GitRepoRepository;

@RestController
@RequestMapping("/api/repos")
@CrossOrigin(origins = "http://localhost:3000") // Allows Next.js to connect
public class RepoController {

    @Autowired
    private GitRepoRepository repoRepository;

    @GetMapping
    public List<GitRepository> getAllRepos() {
        return repoRepository.findAll();
    }

    @PostMapping
    public GitRepository createRepo(@RequestBody GitRepository repo) {
        return repoRepository.save(repo);
    }
}