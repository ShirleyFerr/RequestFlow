package com.requestflow.dto.dashboard;

import com.requestflow.domain.enums.RequestCategory;

public record CategoryDistributionDTO(
        RequestCategory category,
        long total
) {
}
