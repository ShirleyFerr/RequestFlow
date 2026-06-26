package com.requestflow.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank(message = "Nova senha e obrigatoria")
        @Size(min = 6, message = "Nova senha deve ter pelo menos 6 caracteres")
        String newPassword,

        @NotBlank(message = "Confirmacao da senha e obrigatoria")
        String confirmPassword
) {
}
