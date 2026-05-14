package com.myGit.server.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pull_requests")
public class PullRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String status; // "open", "closed", or "merged"
    private int comments;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private GitRepository repository;

    public PullRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = "open";
    }

    public PullRequest(String title, String author, GitRepository repository) {
        this();
        this.title = title;
        this.author = author;
        this.repository = repository;
        this.comments = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}