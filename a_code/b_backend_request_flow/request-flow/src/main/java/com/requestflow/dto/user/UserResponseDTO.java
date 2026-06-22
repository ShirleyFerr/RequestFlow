package com.requestflow.dto.user;

import com.requestflow.domain.enums.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active,
        LocalDateTime createdAt
) {
}
