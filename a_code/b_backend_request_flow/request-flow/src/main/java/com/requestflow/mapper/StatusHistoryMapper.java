package com.requestflow.mapper;

import com.requestflow.domain.entity.StatusHistory;
import com.requestflow.dto.history.StatusHistoryResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatusHistoryMapper {

    @Inject
    UserMapper userMapper;

    public StatusHistoryResponseDTO toResponse(StatusHistory history) {
        if (history == null) {
            return null;
        }

        return new StatusHistoryResponseDTO(
                history.getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                userMapper.toSummary(history.getChangedBy()),
                history.getChangedAt(),
                history.getNote()
        );
    }
}
