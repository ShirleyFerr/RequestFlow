package com.requestflow.dto.dashboard;

public record DashboardSummaryDTO(
        long totalRequests,
        long openRequests,
        long inProgressRequests,
        long waitingInfoRequests,
        long overdueRequests,
        long resolvedRequests,
        long resolvedThisMonthRequests,
        long cancelledRequests,
        double averageResolutionHours
) {
}
