package com.requestflow.dto.request;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;

public record RequestFilterDTO(
        String search,
        RequestStatus status,
        RequestPriority priority,
        RequestCategory category,
        Long requesterId,
        Long assigneeId,
        Boolean overdue,
        Integer page,
        Integer size,
        String sort
) {
}
