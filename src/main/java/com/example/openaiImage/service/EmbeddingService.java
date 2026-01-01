package com.example.openaiImage.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    public List<Double> generateEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "input", text,
                    "model", "text-embedding-3-small"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    EMBEDDING_URL,
                    request,
                    Map.class
            );

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                    return embedding;
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating embedding: " + e.getMessage());
        }
        return List.of();
    }

    public List<String> extractKeywords(String text) {
        String normalized = text.toLowerCase()
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
                .trim();

        return List.of(normalized.split("\\s+"));
    }
}
