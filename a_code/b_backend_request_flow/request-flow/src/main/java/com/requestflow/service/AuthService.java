package com.requestflow.service;

import com.requestflow.domain.entity.User;
import com.requestflow.dto.auth.LoginRequestDTO;
import com.requestflow.dto.auth.LoginResponseDTO;
import com.requestflow.dto.user.UserResponseDTO;
import com.requestflow.exception.ForbiddenException;
import com.requestflow.exception.UnauthorizedException;
import com.requestflow.mapper.UserMapper;
import com.requestflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    UserMapper userMapper;

    @Inject
    CurrentUserService currentUserService;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha invalidos"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ForbiddenException("Usuario inativo");
        }

        if (!passwordService.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("E-mail ou senha invalidos");
        }

        return new LoginResponseDTO(
                jwtTokenService.generateToken(user),
                "Bearer",
                userMapper.toResponse(user)
        );
    }

    public UserResponseDTO me() {
        return userMapper.toResponse(currentUserService.getCurrentUser());
    }
}
