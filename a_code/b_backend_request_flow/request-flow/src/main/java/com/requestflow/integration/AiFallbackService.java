package com.requestflow.integration;

import com.requestflow.domain.enums.AiSuggestionSource;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.dto.ai.AiSuggestionDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.Locale;

@ApplicationScoped
public class AiFallbackService {

    public AiSuggestionDTO suggest(String description) {
        String normalizedDescription = normalize(description);

        if (containsAny(normalizedDescription, "acesso", "login", "senha", "permissao")) {
            return fallback(RequestCategory.ACCESS, RequestPriority.HIGH, description);
        }

        if (containsAny(normalizedDescription, "erro", "bug", "falha", "quebrado")) {
            return fallback(RequestCategory.BUG, RequestPriority.HIGH, description);
        }

        if (containsAny(normalizedDescription, "incidente", "parado", "indisponivel", "producao")) {
            return fallback(RequestCategory.INCIDENT, RequestPriority.CRITICAL, description);
        }

        if (containsAny(normalizedDescription, "melhoria", "funcionalidade", "ajuste")) {
            return fallback(RequestCategory.REQUEST, RequestPriority.MEDIUM, description);
        }

        if (containsAny(normalizedDescription, "duvida", "ajuda", "suporte")) {
            return fallback(RequestCategory.SUPPORT, RequestPriority.MEDIUM, description);
        }

        return fallback(RequestCategory.OTHER, RequestPriority.MEDIUM, description);
    }

    private AiSuggestionDTO fallback(RequestCategory category, RequestPriority priority, String description) {
        return new AiSuggestionDTO(category, priority, buildSummary(description, category, priority), AiSuggestionSource.FALLBACK);
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    private String buildSummary(String description, RequestCategory category, RequestPriority priority) {
        String trimmed = description == null ? "" : description.trim().replaceAll("\\s+", " ");
        if (trimmed.isBlank()) {
            return "Resumo: Solicitacao sem descricao detalhada. Impacto: triagem inicial necessaria.";
        }
        String mainNeed = trimmed.length() <= 110 ? trimmed : trimmed.substring(0, 107) + "...";
        return "Resumo: " + mainNeed + ". Impacto: " + impactText(category, priority) + ".";
    }

    private String impactText(RequestCategory category, RequestPriority priority) {
        if (priority == RequestPriority.CRITICAL || category == RequestCategory.INCIDENT) {
            return "pode afetar a operacao e exige atendimento urgente";
        }
        if (priority == RequestPriority.HIGH) {
            return "pode bloquear o usuario ou reduzir a produtividade";
        }
        if (category == RequestCategory.REQUEST) {
            return "demanda avaliacao e priorizacao pelo time responsavel";
        }
        return "demanda acompanhamento dentro do SLA definido";
    }
}
