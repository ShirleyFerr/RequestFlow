package com.requestflow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @Email(message = "E-mail deve ter formato valido")
        @NotBlank(message = "E-mail e obrigatorio")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, max = 80, message = "Senha deve ter entre 6 e 80 caracteres")
        String password
) {
}
