package com.requestflow.mapper;

import com.requestflow.domain.entity.User;
import com.requestflow.dto.user.UserResponseDTO;
import com.requestflow.dto.user.UserSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public UserSummaryDTO toSummary(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive()
        );
    }

    public UserResponseDTO toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}
