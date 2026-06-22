package com.requestflow.dto.dashboard;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.SlaStatus;

import java.time.LocalDateTime;

public record DashboardRequestItemDTO(
        Long id,
        String title,
        RequestCategory category,
        RequestPriority priority,
        RequestStatus status,
        String requesterName,
        String assigneeName,
        LocalDateTime createdAt,
        LocalDateTime dueDate,
        SlaStatus slaStatus
) {
}
