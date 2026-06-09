package com.myGit.server.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GiteaService {

    private static final Logger logger = LoggerFactory.getLogger(GiteaService.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String adminToken;
    private final String adminUsername;
    private final String adminPassword;

    public GiteaService(RestTemplate restTemplate,
                        @Value("${GITEA_API_URL:http://localhost:3001}") String apiUrl,
                        @Value("${GITEA_ADMIN_TOKEN:}") String adminToken,
                        @Value("${GITEA_ADMIN_USERNAME:}") String adminUsername,
                        @Value("${GITEA_ADMIN_PASSWORD:}") String adminPassword) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.adminToken = adminToken;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public void ensureUserExists(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Gitea username must not be empty");
        }

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("api", "v1", "admin", "users", username)
                .toUriString();

        try {
            getAdminEntity();
            restTemplate.exchange(url, HttpMethod.GET, getAdminEntity(), String.class);
        } catch (HttpClientErrorException.NotFound notFound) {
            createUser(username, email);
        }
    }

    public void createRepository(String owner, String name, String description) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Repository owner must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name must not be empty");
        }

        ensureUserExists(owner, null);

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("api", "v1", "admin", "users", owner, "repos")
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description != null ? description : "");
        body.put("private", false);
        body.put("auto_init", true);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getAdminHeaders());

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException.Conflict conflict) {
            logger.info("Gitea repository already exists: {}/{}", owner, name);
        }
    }

    private void createUser(String username, String email) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("api", "v1", "admin", "users")
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email != null ? email : username + "@example.com");
        body.put("password", generateTemporaryPassword());
        body.put("must_change_password", false);
        body.put("send_notify", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getAdminHeaders());
        restTemplate.postForEntity(url, request, String.class);
        logger.info("Created Gitea user: {}", username);
    }

    private HttpEntity<Void> getAdminEntity() {
        return new HttpEntity<>(getAdminHeaders());
    }

    private HttpHeaders getAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (adminToken != null && !adminToken.isBlank()) {
            headers.setBearerAuth(adminToken);
        } else if (adminUsername != null && !adminUsername.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
            headers.setBasicAuth(adminUsername, adminPassword);
        }
        return headers;
    }

    public String getHttpCloneUrl(String owner, String repoName) {
        return UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment(owner, repoName + ".git")
                .toUriString();
    }

    private String generateTemporaryPassword() {
        return "ChangeMe123!";
    }
}
