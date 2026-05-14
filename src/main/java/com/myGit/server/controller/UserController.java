package com.myGit.server.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myGit.server.repository.UserRepository;

@RestController
@RequestMapping("/api/users") // Base path for all user-related data
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .map(user -> ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "bio", "Full-stack developer and open-source contributor.",
                "followers", 842,
                "following", 128,
                "company", "Acme Corp",
                "location", "Remote"
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}