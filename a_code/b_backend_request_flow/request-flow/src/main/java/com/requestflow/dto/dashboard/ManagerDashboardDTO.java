package com.requestflow.dto.dashboard;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.dto.request.RequestSummaryDTO;

import java.util.List;
import java.util.Map;

public record ManagerDashboardDTO(
        DashboardSummaryDTO summary,
        Map<RequestStatus, Long> byStatus,
        Map<RequestPriority, Long> byPriority,
        Map<RequestCategory, Long> byCategory,
        List<TeamPerformanceDTO> teamPerformance,
        List<RequestSummaryDTO> slaAlerts,
        List<RequestSummaryDTO> criticalOpenRequests,
        List<RequestSummaryDTO> unassignedRequests
) {
}
