package com.requestflow.mapper;

import com.requestflow.domain.entity.RequestComment;
import com.requestflow.dto.comment.CommentResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommentMapper {

    @Inject
    UserMapper userMapper;

    public CommentResponseDTO toResponse(RequestComment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponseDTO(
                comment.getId(),
                userMapper.toSummary(comment.getAuthor()),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}
