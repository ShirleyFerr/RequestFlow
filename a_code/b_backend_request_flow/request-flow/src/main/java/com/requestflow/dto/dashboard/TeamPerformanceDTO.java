package com.requestflow.dto.dashboard;

public record TeamPerformanceDTO(
        Long userId,
        String name,
        String initials,
        long assignedRequests,
        long resolvedThisMonthRequests,
        long overdueRequests,
        double averageResolutionHours
) {
}
