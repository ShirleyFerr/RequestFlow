package com.requestflow.repository;

import com.requestflow.domain.entity.Request;
import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.Role;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class RequestRepository implements PanacheRepository<Request> {

    public RequestPage findVisibleRequests(
            User currentUser,
            RequestStatus status,
            RequestPriority priority,
            RequestCategory category,
            Long assigneeId,
            Long requesterId,
            Boolean overdue,
            int page,
            int size,
            String sort
    ) {
        Parameters params = new Parameters();
        StringBuilder query = new StringBuilder("1 = 1");

        applyVisibility(query, params, currentUser);
        applyFilters(query, params, currentUser, status, priority, category, assigneeId, requesterId, overdue);

        PanacheQuery<Request> panacheQuery = find(query.toString(), resolveSort(sort), params)
                .page(Page.of(page, size));

        return new RequestPage(
                panacheQuery.list(),
                panacheQuery.count()
        );
    }

    private void applyVisibility(StringBuilder query, Parameters params, User currentUser) {
        Role role = currentUser.getRole();

        if (role == Role.USER) {
            query.append(" and requester.id = :currentUserId");
            params.and("currentUserId", currentUser.getId());
            return;
        }

        if (role == Role.ANALYST) {
            query.append(" and (assignee is null or assignee.id = :currentUserId)");
            params.and("currentUserId", currentUser.getId());
        }
    }

    private void applyFilters(
            StringBuilder query,
            Parameters params,
            User currentUser,
            RequestStatus status,
            RequestPriority priority,
            RequestCategory category,
            Long assigneeId,
            Long requesterId,
            Boolean overdue
    ) {
        if (status != null) {
            query.append(" and status = :status");
            params.and("status", status);
        }

        if (currentUser.getRole() != Role.USER && priority != null) {
            query.append(" and priority = :priority");
            params.and("priority", priority);
        }

        if (currentUser.getRole() != Role.USER && category != null) {
            query.append(" and category = :category");
            params.and("category", category);
        }

        if (currentUser.getRole() == Role.MANAGER && assigneeId != null) {
            query.append(" and assignee.id = :assigneeId");
            params.and("assigneeId", assigneeId);
        }

        if (currentUser.getRole() == Role.MANAGER && requesterId != null) {
            query.append(" and requester.id = :requesterId");
            params.and("requesterId", requesterId);
        }

        if (currentUser.getRole() == Role.MANAGER && Boolean.TRUE.equals(overdue)) {
            query.append(" and dueDate < :now");
            query.append(" and status not in (:resolvedStatus, :cancelledStatus)");
            params.and("now", LocalDateTime.now());
            params.and("resolvedStatus", RequestStatus.RESOLVED);
            params.and("cancelledStatus", RequestStatus.CANCELLED);
        }
    }

    private Sort resolveSort(String sort) {
        if ("createdAt,asc".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").ascending();
        }
        if ("createdAt,desc".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").descending();
        }
        if ("dueDate,desc".equalsIgnoreCase(sort)) {
            return Sort.by("dueDate").descending();
        }
        return Sort.by("dueDate").ascending();
    }

    public record RequestPage(List<Request> content, long totalElements) {
    }

    public List<Request> findAssignedTo(User analyst) {
        return find("assignee.id = ?1", analyst.getId()).list();
    }

    public List<Request> findActiveAssignedTo(User analyst) {
        return find(
                "assignee.id = ?1 and status not in (?2, ?3)",
                Sort.by("dueDate").ascending(),
                analyst.getId(),
                RequestStatus.RESOLVED,
                RequestStatus.CANCELLED
        ).list();
    }

    public List<Request> findOverdueAssignedTo(User analyst, LocalDateTime now) {
        return find(
                "assignee.id = ?1 and dueDate < ?2 and status not in (?3, ?4)",
                analyst.getId(),
                now,
                RequestStatus.RESOLVED,
                RequestStatus.CANCELLED
        ).list();
    }

    public List<Request> findResolvedAssignedToSince(User analyst, LocalDateTime startDate) {
        return find(
                "assignee.id = ?1 and status = ?2 and resolvedAt >= ?3",
                analyst.getId(),
                RequestStatus.RESOLVED,
                startDate
        ).list();
    }

    public List<Request> findAllForDashboard() {
        return findAll().list();
    }

    public List<Request> findOverdue(LocalDateTime now) {
        return find(
                "dueDate < ?1 and status not in (?2, ?3)",
                Sort.by("dueDate").ascending(),
                now,
                RequestStatus.RESOLVED,
                RequestStatus.CANCELLED
        ).list();
    }

    public List<Request> findCriticalOpen() {
        return find(
                "priority = ?1 and status not in (?2, ?3)",
                Sort.by("dueDate").ascending(),
                RequestPriority.CRITICAL,
                RequestStatus.RESOLVED,
                RequestStatus.CANCELLED
        ).list();
    }

    public List<Request> findUnassignedActive() {
        return find(
                "assignee is null and status not in (?1, ?2)",
                Sort.by("dueDate").ascending(),
                RequestStatus.RESOLVED,
                RequestStatus.CANCELLED
        ).list();
    }

    public List<Request> findResolvedSince(LocalDateTime startDate) {
        return find("status = ?1 and resolvedAt >= ?2", RequestStatus.RESOLVED, startDate).list();
    }

    public List<Request> findAssignedToSince(User analyst, LocalDateTime startDate) {
        return find(
                "assignee.id = ?1 and resolvedAt >= ?2",
                analyst.getId(),
                startDate
        ).list();
    }
}
