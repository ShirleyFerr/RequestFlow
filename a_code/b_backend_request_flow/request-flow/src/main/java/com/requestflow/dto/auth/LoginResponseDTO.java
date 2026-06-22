package com.requestflow.dto.auth;

import com.requestflow.dto.user.UserResponseDTO;

public record LoginResponseDTO(
        String token,
        String tokenType,
        UserResponseDTO user
) {
}
