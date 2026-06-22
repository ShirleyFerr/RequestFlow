package com.requestflow.dto.user;

import com.requestflow.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String name,

        @Email(message = "E-mail deve ter formato valido")
        @NotBlank(message = "E-mail e obrigatorio")
        @Size(max = 180, message = "E-mail deve ter no maximo 180 caracteres")
        String email,

        @NotNull(message = "Perfil e obrigatorio")
        Role role,

        @NotNull(message = "Data de nascimento e obrigatoria")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate birthDate,

        @NotNull(message = "Indicador de usuario ativo e obrigatorio")
        Boolean active
) {
}
