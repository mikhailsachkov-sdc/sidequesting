package org.sideqquest.sidequestai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

@Component
public class AnthropicHaikuClient {

    private static final String API_KEY_FILE = "anthropic-api.properties";
    private static final String API_KEY_PROPERTY = "anthropic.api.key";
    private static final String DEFAULT_MODEL = "claude-haiku-4-5-latest";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AnthropicHaikuClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.anthropic.com").build();
        this.objectMapper = objectMapper;
        this.apiKey = loadApiKey();
    }

    public String ask(String prompt) {
        return ask(prompt, 512);
    }

    public String ask(String prompt, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be greater than 0");
        }

        Map<String, Object> payload = Map.of(
                "model", DEFAULT_MODEL,
                "max_tokens", maxTokens,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        String rawResponse = webClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException("Anthropic API returned an empty response");
        }

        return extractText(rawResponse);
    }

    private String extractText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentArray = root.path("content");

            if (!contentArray.isArray() || contentArray.isEmpty()) {
                throw new IllegalStateException("Anthropic response does not contain content");
            }

            for (JsonNode contentItem : contentArray) {
                if ("text".equals(contentItem.path("type").asText())) {
                    String text = contentItem.path("text").asText();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }

            throw new IllegalStateException("Anthropic response has no text content");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Anthropic response", e);
        }
    }

    private String loadApiKey() {
        Path configPath = Path.of(API_KEY_FILE);
        if (!Files.exists(configPath)) {
            throw new IllegalStateException(
                    "Missing " + API_KEY_FILE + ". Copy anthropic-api.properties.example and set your API key."
            );
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + API_KEY_FILE, e);
        }

        String key = properties.getProperty(API_KEY_PROPERTY);
        if (key == null || key.isBlank() || key.contains("replace-with-your-anthropic-api-key")) {
            throw new IllegalStateException(
                    "Set a valid " + API_KEY_PROPERTY + " in " + API_KEY_FILE
            );
        }

        return key.trim();
    }
}
