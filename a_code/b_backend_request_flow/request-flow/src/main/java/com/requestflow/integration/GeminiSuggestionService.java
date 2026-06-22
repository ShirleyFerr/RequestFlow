package com.requestflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.requestflow.domain.enums.AiSuggestionSource;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.dto.ai.AiSuggestionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class GeminiSuggestionService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @ConfigProperty(name = "requestflow.ai.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "requestflow.ai.model", defaultValue = "gemini-2.5-flash")
    String model;

    @ConfigProperty(name = "requestflow.ai.timeout.ms", defaultValue = "3000")
    long timeoutMs;

    @Inject
    ObjectMapper objectMapper;

    GeminiHttpTransport transport;

    public GeminiSuggestionService() {
        this.transport = request -> HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(resolveTimeoutMs()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    public GeminiSuggestionService(
            ObjectMapper objectMapper,
            String apiKey,
            String model,
            long timeoutMs,
            GeminiHttpTransport transport
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = Optional.ofNullable(apiKey);
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.transport = transport;
    }

    public AiSuggestionDTO suggest(String description) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiUrl()))
                    .timeout(Duration.ofMillis(resolveTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey.get())
                    .POST(HttpRequest.BodyPublishers.ofString(geminiPayload(description)))
                    .build();

            HttpResponse<String> response = sendWithTimeout(request);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini returned non-success status");
            }

            return parseGeminiResponse(response.body());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not obtain Gemini suggestion", exception);
        }
    }

    private String geminiUrl() {
        return GEMINI_BASE_URL + safeModel() + ":generateContent";
    }

    private String safeModel() {
        return model == null || model.isBlank() ? "gemini-2.5-flash" : model.trim();
    }

    private HttpResponse<String> sendWithTimeout(HttpRequest request) throws Exception {
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return transport.send(request);
                    } catch (Exception exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .orTimeout(resolveTimeoutMs(), TimeUnit.MILLISECONDS)
                .get();
    }

    private long resolveTimeoutMs() {
        return timeoutMs <= 0 ? 1200 : timeoutMs;
    }

    public String geminiPayload(String description) throws Exception {
        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt(description)))
                )),
                "generationConfig", Map.of(
                        "responseFormat", Map.of(
                                "text", Map.of(
                                        "mimeType", "application/json",
                                        "schema", responseSchema()
                                )
                        )
                )
        );
        return objectMapper.writeValueAsString(payload);
    }

    String prompt(String description) {
        return """
                Classifique a solicitacao interna abaixo.

                Retorne apenas JSON valido com:
                category: ACCESS, BUG, REQUEST, INCIDENT, SUPPORT ou OTHER
                priority: LOW, MEDIUM, HIGH ou CRITICAL
                summary: resumo curto, claro e profissional, com no maximo duas frases.
                Escreva no mesmo idioma da descricao recebida. Se a descricao estiver em portugues,
                use portugues correto, com boa gramatica, acentuacao e termos corporativos simples.
                Estruture como:
                "Resumo: <necessidade principal>. Impacto: <risco ou urgencia percebida>."

                Descricao:
                %s
                """.formatted(description == null ? "" : description);
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "category", Map.of(
                                "type", "string",
                                "enum", List.of("ACCESS", "BUG", "REQUEST", "INCIDENT", "SUPPORT", "OTHER")
                        ),
                        "priority", Map.of(
                                "type", "string",
                                "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")
                        ),
                        "summary", Map.of(
                                "type", "string",
                                "description", "Resumo curto em portugues"
                        )
                ),
                "required", List.of("category", "priority", "summary")
        );
    }

    AiSuggestionDTO parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);

        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini response did not contain text");
        }

        JsonNode suggestion = objectMapper.readTree(text);
        RequestCategory category = parseCategory(suggestion.path("category").asText(null));
        RequestPriority priority = parsePriority(suggestion.path("priority").asText(null));
        String summary = suggestion.path("summary").asText(null);

        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("Gemini response did not contain summary");
        }

        return new AiSuggestionDTO(category, priority, summary.trim(), AiSuggestionSource.AI);
    }

    private RequestCategory parseCategory(String value) {
        try {
            return RequestCategory.valueOf(normalizeEnum(value));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Gemini response contained invalid category", exception);
        }
    }

    private RequestPriority parsePriority(String value) {
        try {
            return RequestPriority.valueOf(normalizeEnum(value));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Gemini response contained invalid priority", exception);
        }
    }

    private String normalizeEnum(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Gemini response contained blank enum value");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
