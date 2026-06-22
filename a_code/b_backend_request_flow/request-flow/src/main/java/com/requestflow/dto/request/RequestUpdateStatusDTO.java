package com.requestflow.dto.request;

import com.requestflow.domain.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestUpdateStatusDTO(
        @NotNull(message = "Novo status e obrigatorio")
        RequestStatus newStatus,

        @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
        String note
) {
}
