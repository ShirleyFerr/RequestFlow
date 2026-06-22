package com.requestflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateDTO(
        @NotBlank(message = "Comentario e obrigatorio")
        @Size(max = 2000, message = "Comentario deve ter no maximo 2000 caracteres")
        String message
) {
}
