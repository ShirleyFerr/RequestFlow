package com.requestflow.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserActiveUpdateDTO(
        @NotNull(message = "Indicador de usuario ativo e obrigatorio")
        Boolean active
) {
}
