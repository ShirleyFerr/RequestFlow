package com.requestflow.dto.dashboard;

public record DashboardKpiDTO(
        String key,
        String label,
        long value
) {
}
