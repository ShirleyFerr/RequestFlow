package com.requestflow.dto.user;

import com.requestflow.domain.enums.Role;

public record UserSummaryDTO(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active
) {
}
