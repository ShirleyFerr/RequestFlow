package com.requestflow.service;

import com.requestflow.domain.entity.Request;
import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.Role;
import com.requestflow.dto.dashboard.AnalystDashboardDTO;
import com.requestflow.dto.dashboard.DashboardSummaryDTO;
import com.requestflow.dto.dashboard.ManagerDashboardDTO;
import com.requestflow.dto.dashboard.TeamPerformanceDTO;
import com.requestflow.dto.request.RequestSummaryDTO;
import com.requestflow.exception.ForbiddenException;
import com.requestflow.mapper.RequestMapper;
import com.requestflow.repository.RequestRepository;
import com.requestflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DashboardService {

    private static final int ALERT_LIMIT = 10;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    RequestRepository requestRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    RequestMapper requestMapper;

    @Transactional(Transactional.TxType.SUPPORTS)
    public Object getDashboardForCurrentUser() {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.ANALYST) {
            return getAnalystDashboard(currentUser);
        }
        if (currentUser.getRole() == Role.MANAGER) {
            return getManagerDashboard(currentUser);
        }
        throw new ForbiddenException("Solicitantes nao possuem dashboard");
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public AnalystDashboardDTO getAnalystDashboard() {
        return getAnalystDashboard(currentUserService.getCurrentUser());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public ManagerDashboardDTO getManagerDashboard() {
        return getManagerDashboard(currentUserService.getCurrentUser());
    }

    private AnalystDashboardDTO getAnalystDashboard(User analyst) {
        if (analyst.getRole() != Role.ANALYST) {
            throw new ForbiddenException("Dashboard operacional disponivel apenas para analistas");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = startOfCurrentMonth();
        List<Request> assigned = requestRepository.findAssignedTo(analyst);
        List<Request> activeQueue = requestRepository.findActiveAssignedTo(analyst).stream()
                .sorted(workQueueComparator())
                .toList();
        List<Request> overdue = requestRepository.findOverdueAssignedTo(analyst, now).stream()
                .sorted(Comparator.comparing(Request::getDueDate))
                .toList();
        List<Request> resolvedThisMonth = requestRepository.findResolvedAssignedToSince(analyst, startOfMonth);
        List<Request> criticalOrHigh = activeQueue.stream()
                .filter(this::isHighOrCritical)
                .toList();

        return new AnalystDashboardDTO(
                buildSummary(assigned),
                assigned.size(),
                overdue.size(),
                criticalOrHigh.size(),
                resolvedThisMonth.size(),
                averageResolutionHours(resolvedThisMonth),
                toSummaryList(activeQueue, ALERT_LIMIT),
                toSummaryList(overdue, ALERT_LIMIT),
                toSummaryList(criticalOrHigh, ALERT_LIMIT)
        );
    }

    private ManagerDashboardDTO getManagerDashboard(User manager) {
        if (manager.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Dashboard gerencial disponivel apenas para gestores");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Request> allRequests = requestRepository.findAllForDashboard();
        List<Request> overdue = requestRepository.findOverdue(now);
        List<Request> criticalOpen = requestRepository.findCriticalOpen();
        List<Request> unassigned = requestRepository.findUnassignedActive();

        return new ManagerDashboardDTO(
                buildSummary(allRequests),
                countByStatus(allRequests),
                countByPriority(allRequests),
                countByCategory(allRequests),
                buildTeamPerformance(now),
                toSummaryList(overdue, ALERT_LIMIT),
                toSummaryList(criticalOpen, ALERT_LIMIT),
                toSummaryList(unassigned, ALERT_LIMIT)
        );
    }

    private DashboardSummaryDTO buildSummary(List<Request> requests) {
        LocalDateTime startOfMonth = startOfCurrentMonth();
        List<Request> resolvedThisMonth = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.RESOLVED)
                .filter(request -> request.getResolvedAt() != null && !request.getResolvedAt().isBefore(startOfMonth))
                .toList();

        return new DashboardSummaryDTO(
                requests.size(),
                countByStatus(requests, RequestStatus.OPEN),
                countByStatus(requests, RequestStatus.IN_PROGRESS),
                countByStatus(requests, RequestStatus.WAITING_INFO),
                requests.stream().filter(this::isOverdue).count(),
                countByStatus(requests, RequestStatus.RESOLVED),
                resolvedThisMonth.size(),
                countByStatus(requests, RequestStatus.CANCELLED),
                averageResolutionHours(resolvedThisMonth)
        );
    }

    private List<TeamPerformanceDTO> buildTeamPerformance(LocalDateTime now) {
        LocalDateTime startOfMonth = startOfCurrentMonth();
        return userRepository.findActiveAnalysts().stream()
                .map(analyst -> {
                    List<Request> assigned = requestRepository.findAssignedTo(analyst);
                    List<Request> resolvedThisMonth = requestRepository.findResolvedAssignedToSince(analyst, startOfMonth);
                    List<Request> overdue = requestRepository.findOverdueAssignedTo(analyst, now);

                    return new TeamPerformanceDTO(
                            analyst.getId(),
                            analyst.getName(),
                            initials(analyst.getName()),
                            assigned.size(),
                            resolvedThisMonth.size(),
                            overdue.size(),
                            averageResolutionHours(resolvedThisMonth)
                    );
                })
                .toList();
    }

    private Map<RequestStatus, Long> countByStatus(List<Request> requests) {
        Map<RequestStatus, Long> result = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            result.put(status, countByStatus(requests, status));
        }
        return result;
    }

    private Map<RequestPriority, Long> countByPriority(List<Request> requests) {
        Map<RequestPriority, Long> result = new EnumMap<>(RequestPriority.class);
        for (RequestPriority priority : RequestPriority.values()) {
            result.put(priority, requests.stream().filter(request -> request.getPriority() == priority).count());
        }
        return result;
    }

    private Map<RequestCategory, Long> countByCategory(List<Request> requests) {
        Map<RequestCategory, Long> result = new EnumMap<>(RequestCategory.class);
        for (RequestCategory category : RequestCategory.values()) {
            result.put(category, requests.stream().filter(request -> request.getCategory() == category).count());
        }
        return result;
    }

    private long countByStatus(List<Request> requests, RequestStatus status) {
        return requests.stream()
                .filter(request -> request.getStatus() == status)
                .count();
    }

    private List<RequestSummaryDTO> toSummaryList(List<Request> requests, int limit) {
        return requests.stream()
                .limit(limit)
                .map(requestMapper::toSummary)
                .toList();
    }

    private Comparator<Request> workQueueComparator() {
        return Comparator
                .comparingInt((Request request) -> priorityWeight(request.getPriority()))
                .thenComparing(Request::getDueDate);
    }

    private int priorityWeight(RequestPriority priority) {
        return switch (priority) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private boolean isHighOrCritical(Request request) {
        return request.getPriority() == RequestPriority.CRITICAL || request.getPriority() == RequestPriority.HIGH;
    }

    private boolean isOverdue(Request request) {
        return request.getDueDate() != null
                && request.getDueDate().isBefore(LocalDateTime.now())
                && request.getStatus() != RequestStatus.RESOLVED
                && request.getStatus() != RequestStatus.CANCELLED;
    }

    private double averageResolutionHours(List<Request> requests) {
        List<Request> resolved = requests.stream()
                .filter(request -> request.getCreatedAt() != null && request.getResolvedAt() != null)
                .toList();

        if (resolved.isEmpty()) {
            return 0;
        }

        double average = resolved.stream()
                .mapToLong(request -> Duration.between(request.getCreatedAt(), request.getResolvedAt()).toMinutes())
                .average()
                .orElse(0);

        return Math.round((average / 60.0) * 100.0) / 100.0;
    }

    private LocalDateTime startOfCurrentMonth() {
        return YearMonth.now().atDay(1).atStartOfDay();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
