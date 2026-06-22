package com.requestflow.repository;

import com.requestflow.domain.entity.RequestComment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RequestCommentRepository implements PanacheRepository<RequestComment> {
}
