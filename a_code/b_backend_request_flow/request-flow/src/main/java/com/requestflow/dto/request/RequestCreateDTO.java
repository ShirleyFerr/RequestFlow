package com.requestflow.dto.request;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RequestCreateDTO(
        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 180, message = "Titulo deve ter no maximo 180 caracteres")
        String title,

        @NotBlank(message = "Descricao e obrigatoria")
        @Size(max = 5000, message = "Descricao deve ter no maximo 5000 caracteres")
        String description,

        @NotNull(message = "Categoria e obrigatoria")
        RequestCategory category,

        @NotNull(message = "Prioridade e obrigatoria")
        RequestPriority priority,

        @NotNull(message = "Data de entrega e obrigatoria")
        @FutureOrPresent(message = "Data de entrega deve ser atual ou futura")
        LocalDateTime dueDate,

        @Size(max = 5000, message = "Resumo da IA deve ter no maximo 5000 caracteres")
        String aiSummary
) {
}
