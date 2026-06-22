package com.requestflow.dto.dashboard;

import com.requestflow.domain.enums.RequestStatus;

public record StatusDistributionDTO(
        RequestStatus status,
        long total
) {
}
