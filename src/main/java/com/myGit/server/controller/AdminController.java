package com.myGit.server.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myGit.server.model.User;
import com.myGit.server.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllSystemUsers(Authentication authentication) {
        // שליפת המשתמש המחובר לפי האימייל שנשמר ב-SecurityContext
        Optional<User> currentUserOpt = userRepository.findByEmail(authentication.getName());
        
        // וידוא שהמשתמש קיים ושיש לו הרשאת אדמין
        if (currentUserOpt.isEmpty() || !"ROLE_ADMIN".equals(currentUserOpt.get().getRole())) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Access Denied: Admin role required."));
        }

        // אם המשתמש הוא אדמין, נחזיר את הרשומות המבוקשות (למשל, כלל המשתמשים)
        return ResponseEntity.ok(userRepository.findAll());
    }
}