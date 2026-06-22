package com.requestflow.integration;

import com.requestflow.dto.ai.AiSuggestionDTO;
import com.requestflow.dto.ai.AiSuggestionRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class AiProviderRouter implements AiService {

    private static final Logger LOG = Logger.getLogger(AiProviderRouter.class);
    private static final String GEMINI_PROVIDER = "gemini";

    @ConfigProperty(name = "requestflow.ai.provider", defaultValue = "fallback")
    String provider;

    @ConfigProperty(name = "requestflow.ai.api-key")
    Optional<String> apiKey;

    @Inject
    GeminiSuggestionService geminiSuggestionService;

    @Inject
    AiFallbackService fallbackService;

    @Override
    public AiSuggestionDTO suggest(AiSuggestionRequestDTO request) {
        String description = request.description();

        if (!GEMINI_PROVIDER.equals(normalizedProvider()) || apiKey.isEmpty() || apiKey.get().isBlank()) {
            return fallbackService.suggest(description);
        }

        try {
            return geminiSuggestionService.suggest(description);
        } catch (RuntimeException exception) {
            LOG.warn("Gemini suggestion failed. Falling back to local classifier.");
            return fallbackService.suggest(description);
        }
    }

    private String normalizedProvider() {
        return provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
    }
}
