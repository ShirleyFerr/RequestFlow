package com.requestflow.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiSuggestionRequestDTO(
        @NotBlank(message = "Descricao e obrigatoria para gerar sugestao")
        @Size(max = 5000, message = "Descricao deve ter no maximo 5000 caracteres")
        String description
) {
}
