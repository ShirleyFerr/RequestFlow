package com.requestflow.dto.request;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.SlaStatus;
import com.requestflow.dto.user.UserSummaryDTO;

import java.time.LocalDateTime;

public record RequestSummaryDTO(
        Long id,
        String title,
        RequestCategory category,
        RequestPriority priority,
        RequestStatus status,
        UserSummaryDTO requester,
        UserSummaryDTO assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dueDate,
        LocalDateTime resolvedAt,
        SlaStatus slaStatus,
        Boolean overdue
) {
}
