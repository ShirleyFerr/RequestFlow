package com.requestflow.repository;

import com.requestflow.domain.entity.StatusHistory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatusHistoryRepository implements PanacheRepository<StatusHistory> {
}
