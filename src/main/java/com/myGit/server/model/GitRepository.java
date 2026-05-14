package com.myGit.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "repositories")
public class GitRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String owner;
    private int stars;
    private String language;

    // 1. No-args constructor (Required by JPA)
    public GitRepository() {
        
    }

    // 2. All-args constructor
    public GitRepository(Long id, String name, String description, String owner, String language, int stars) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.language = language;
        this.stars = stars;
    }

    // 3. Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
    
    public String getLanguage() { 
        return language; 
    }
    public void setLanguage(String language) { 
        this.language = language; 
    }
}