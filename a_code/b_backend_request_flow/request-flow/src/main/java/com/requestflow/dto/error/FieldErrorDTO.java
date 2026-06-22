package com.requestflow.dto.error;

public record FieldErrorDTO(
        String field,
        String message
) {
}
