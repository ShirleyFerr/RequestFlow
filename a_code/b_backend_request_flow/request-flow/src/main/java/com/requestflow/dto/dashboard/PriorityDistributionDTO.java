package com.requestflow.dto.dashboard;

import com.requestflow.domain.enums.RequestPriority;

public record PriorityDistributionDTO(
        RequestPriority priority,
        long total
) {
}
