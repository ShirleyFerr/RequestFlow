package com.requestflow.dto.history;

import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.dto.user.UserSummaryDTO;

import java.time.LocalDateTime;

public record StatusHistoryResponseDTO(
        Long id,
        RequestStatus oldStatus,
        RequestStatus newStatus,
        UserSummaryDTO changedBy,
        LocalDateTime changedAt,
        String note
) {
}
