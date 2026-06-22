package com.requestflow.repository;

import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return find("lower(email) = ?1", email.toLowerCase()).firstResultOptional();
    }

    public List<User> findAllFiltered(Role role, Boolean active) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder("1 = 1");

        if (role != null) {
            query.append(" and role = :role");
            params.put("role", role);
        }

        if (active != null) {
            query.append(" and active = :active");
            params.put("active", active);
        }

        return find(query.toString(), params)
                .list();
    }

    public List<User> findActiveAnalysts() {
        return find("role = ?1 and active = true", Role.ANALYST).list();
    }
}
