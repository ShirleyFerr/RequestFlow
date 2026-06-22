package com.requestflow.dto.request;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.dto.comment.CommentResponseDTO;
import com.requestflow.dto.history.StatusHistoryResponseDTO;
import com.requestflow.dto.user.UserSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;

public record RequestResponseDTO(
        Long id,
        String title,
        String description,
        RequestCategory category,
        RequestPriority priority,
        RequestStatus status,
        UserSummaryDTO requester,
        UserSummaryDTO assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dueDate,
        LocalDateTime resolvedAt,
        String aiSummary,
        List<CommentResponseDTO> comments,
        List<StatusHistoryResponseDTO> statusHistory
) {
}
