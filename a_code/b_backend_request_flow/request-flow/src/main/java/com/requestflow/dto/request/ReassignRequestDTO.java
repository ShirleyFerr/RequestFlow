package com.requestflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReassignRequestDTO(
        @NotNull(message = "Responsavel e obrigatorio")
        Long assigneeId,

        @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
        String note
) {
}
