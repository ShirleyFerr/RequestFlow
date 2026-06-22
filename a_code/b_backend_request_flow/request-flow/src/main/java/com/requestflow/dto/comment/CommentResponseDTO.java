package com.requestflow.dto.comment;

import com.requestflow.dto.user.UserSummaryDTO;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        UserSummaryDTO author,
        String message,
        LocalDateTime createdAt
) {
}
