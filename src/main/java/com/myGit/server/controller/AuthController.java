package com.myGit.server.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myGit.server.model.User;
import com.myGit.server.repository.UserRepository;
import com.myGit.server.security.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Matches your Next.js port
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        return userRepository.findByEmail(loginRequest.get("email"))
                .filter(user -> passwordEncoder.matches(loginRequest.get("password"), user.getPassword()))
                .map(user -> {
                    String token = jwtUtils.generateToken(user.getEmail());
                    return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername()));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid credentials")));
    }

    @PostMapping("/guest")
    public ResponseEntity<?> guestLogin() {
    // Generate a token for a generic guest identity
    String token = jwtUtils.generateToken("guest@mygit.com");
    return ResponseEntity.ok(Map.of(
        "token", token,
        "username", "GuestUser",
        "isGuest", true
    ));
    }
}