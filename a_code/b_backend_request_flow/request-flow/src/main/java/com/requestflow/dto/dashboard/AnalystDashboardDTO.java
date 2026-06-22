package com.requestflow.dto.dashboard;

import com.requestflow.dto.request.RequestSummaryDTO;

import java.util.List;

public record AnalystDashboardDTO(
        DashboardSummaryDTO summary,
        long assignedToMe,
        long overdueAssignedToMe,
        long highPriorityAssignedToMe,
        long resolvedByMe,
        double averageResolutionHours,
        List<RequestSummaryDTO> workQueue,
        List<RequestSummaryDTO> slaAlerts,
        List<RequestSummaryDTO> criticalOrHighPriority
) {
}
