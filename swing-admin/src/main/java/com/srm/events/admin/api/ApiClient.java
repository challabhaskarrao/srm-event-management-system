package com.srm.events.admin.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private static final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String authToken;
    private String currentUserEmail;
    private String currentUserName;
    private String currentUserRole;
    private Long currentUserId;

    private ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public void login(String email, String password) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            String role = rootNode.get("role").asText();
            
            if ("STUDENT".equals(role)) {
                throw new RuntimeException("Access Denied: Students must use the Web Portal.");
            }
            
            this.authToken = rootNode.get("token").asText();
            this.currentUserEmail = rootNode.get("email").asText();
            this.currentUserName = this.currentUserEmail;
            this.currentUserRole = role;
            this.currentUserId = rootNode.get("userId").asLong();
        } else {
            throw new RuntimeException("Login failed. Check your credentials.");
        }
    }

    public List<Map<String, Object>> getReviewQueue(String role) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/applications/review-queue?role=" + role))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new RuntimeException("Failed to fetch review queue: " + response.statusCode());
        }
    }

    public List<Map<String, Object>> getAllApplications() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/applications/all"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new RuntimeException("Failed to fetch all applications: " + response.statusCode());
        }
    }

    public void submitReview(Long appId, String status, String remarks) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(Map.of(
                "status", status,
                "remarks", remarks
        ));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/applications/" + appId + "/review"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to submit review: " + response.statusCode());
        }
    }

    public void logout() {
        this.authToken = null;
        this.currentUserEmail = null;
        this.currentUserName = null;
        this.currentUserRole = null;
        this.currentUserId = null;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }
}
