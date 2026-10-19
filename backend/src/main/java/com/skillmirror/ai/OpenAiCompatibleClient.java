package com.skillmirror.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.config.SkillMirrorProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for any OpenAI-compatible chat completions API (OpenAI, Groq, Mistral, Ollama...).
 * Configured exclusively through environment variables — never exposed to the frontend.
 */
@Component
public class OpenAiCompatibleClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleClient.class);

    private final SkillMirrorProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiCompatibleClient(SkillMirrorProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(Math.max(5, properties.getAi().getTimeoutSeconds() / 3)));
        factory.setReadTimeout(Duration.ofSeconds(properties.getAi().getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.getAi().getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public boolean supportsLiveCalls() {
        return properties.getAi().hasKey();
    }

    @Override
    public AiCompletion complete(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        SkillMirrorProperties.Ai ai = properties.getAi();
        Map<String, Object> body = Map.of(
                "model", ai.getModel(),
                "temperature", 0.2,
                "max_tokens", ai.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)));

        try {
            String raw = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + ai.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new AiException("Réponse vide du fournisseur IA.");
            }
            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            return new AiCompletion(content, promptTokens, completionTokens, ai.getModel(),
                    System.currentTimeMillis() - start);
        } catch (AiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("AI call failed: {}", ex.getMessage());
            throw new AiException("Appel IA indisponible : " + ex.getMessage(), ex);
        }
    }

    /** Extracts a JSON object from a model answer, tolerating markdown fences and surrounding text. */
    public static String extractJsonObject(String raw) {
        if (raw == null) return "";
        String text = raw.trim();
        int fenceStart = text.indexOf("```");
        if (fenceStart >= 0) {
            int fenceEnd = text.indexOf("```", fenceStart + 3);
            if (fenceEnd > fenceStart) {
                text = text.substring(fenceStart + 3, fenceEnd).trim();
                if (text.startsWith("json")) text = text.substring(4).trim();
            }
        }
        int open = text.indexOf('{');
        int close = text.lastIndexOf('}');
        if (open >= 0 && close > open) {
            return text.substring(open, close + 1);
        }
        return text;
    }
}
