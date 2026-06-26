package com.requestflow.dto.profile;

import com.requestflow.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProfileResponseDTO(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDate birthDate
) {
}
