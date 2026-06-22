package com.requestflow.service;

import com.requestflow.domain.entity.User;
import com.requestflow.exception.UnauthorizedException;
import com.requestflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class CurrentUserService {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    public User getCurrentUser() {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnauthorizedException("Token ausente ou invalido");
        }

        Long userId = parseUserId(jwt.getSubject());
        return userRepository.findByIdOptional(userId)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .orElseThrow(() -> new UnauthorizedException("Usuario autenticado nao encontrado ou inativo"));
    }

    private Long parseUserId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new UnauthorizedException("Token invalido");
        }
    }
}
