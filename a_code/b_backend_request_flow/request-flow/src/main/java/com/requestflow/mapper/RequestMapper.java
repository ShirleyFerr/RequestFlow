package com.requestflow.mapper;

import com.requestflow.domain.entity.Request;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.SlaStatus;
import com.requestflow.dto.comment.CommentResponseDTO;
import com.requestflow.dto.history.StatusHistoryResponseDTO;
import com.requestflow.dto.request.RequestResponseDTO;
import com.requestflow.dto.request.RequestSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class RequestMapper {

    @Inject
    UserMapper userMapper;

    @Inject
    CommentMapper commentMapper;

    @Inject
    StatusHistoryMapper statusHistoryMapper;

    public RequestSummaryDTO toSummary(Request request) {
        if (request == null) {
            return null;
        }

        return new RequestSummaryDTO(
                request.getId(),
                request.getTitle(),
                request.getCategory(),
                request.getPriority(),
                request.getStatus(),
                userMapper.toSummary(request.getRequester()),
                userMapper.toSummary(request.getAssignee()),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getDueDate(),
                request.getResolvedAt(),
                getSlaStatus(request),
                isOverdue(request)
        );
    }

    public RequestResponseDTO toResponse(Request request) {
        if (request == null) {
            return null;
        }

        List<CommentResponseDTO> comments = request.getComments().stream()
                .sorted(Comparator.comparing(comment -> comment.getCreatedAt()))
                .map(commentMapper::toResponse)
                .toList();

        List<StatusHistoryResponseDTO> statusHistory = request.getStatusHistory().stream()
                .sorted(Comparator.comparing(history -> history.getChangedAt()))
                .map(statusHistoryMapper::toResponse)
                .toList();

        return new RequestResponseDTO(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getPriority(),
                request.getStatus(),
                userMapper.toSummary(request.getRequester()),
                userMapper.toSummary(request.getAssignee()),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getDueDate(),
                request.getResolvedAt(),
                request.getAiSummary(),
                comments,
                statusHistory
        );
    }

    private SlaStatus getSlaStatus(Request request) {
        if (request.getStatus() == RequestStatus.CANCELLED) {
            return SlaStatus.CANCELLED;
        }
        if (request.getStatus() == RequestStatus.RESOLVED) {
            return SlaStatus.RESOLVED;
        }
        if (isOverdue(request)) {
            return SlaStatus.OVERDUE;
        }
        return SlaStatus.ON_TIME;
    }

    private boolean isOverdue(Request request) {
        return request.getDueDate() != null
                && request.getDueDate().isBefore(java.time.LocalDateTime.now())
                && request.getStatus() != RequestStatus.RESOLVED
                && request.getStatus() != RequestStatus.CANCELLED;
    }
}
