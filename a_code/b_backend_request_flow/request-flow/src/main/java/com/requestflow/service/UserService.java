package com.requestflow.service;

import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.Role;
import com.requestflow.dto.user.UserActiveUpdateDTO;
import com.requestflow.dto.user.UserCreateRequestDTO;
import com.requestflow.dto.user.UserResponseDTO;
import com.requestflow.dto.user.UserSummaryDTO;
import com.requestflow.exception.ConflictException;
import com.requestflow.exception.NotFoundException;
import com.requestflow.mapper.UserMapper;
import com.requestflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class UserService {

    private static final DateTimeFormatter INITIAL_PASSWORD_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    UserMapper userMapper;

    public List<UserResponseDTO> list(Role role, Boolean active) {
        return userRepository.findAllFiltered(role, active).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        return userMapper.toResponse(findEntityById(id));
    }

    public List<UserSummaryDTO> listActiveAnalysts() {
        return userRepository.findActiveAnalysts().stream()
                .map(userMapper::toSummary)
                .toList();
    }

    @Transactional
    public UserResponseDTO create(UserCreateRequestDTO request) {
        userRepository.findByEmail(request.email())
                .ifPresent(existingUser -> {
                    throw new ConflictException("Ja existe usuario cadastrado com este e-mail");
                });

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setRole(request.role());
        user.setBirthDate(request.birthDate());
        user.setActive(request.active() != null ? request.active() : true);
        user.setPasswordHash(passwordService.hash(generateInitialPassword(request)));

        userRepository.persist(user);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDTO updateActive(Long id, UserActiveUpdateDTO request) {
        User user = findEntityById(id);
        user.setActive(request.active());

        return userMapper.toResponse(user);
    }

    private User findEntityById(Long id) {
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
    }

    private String generateInitialPassword(UserCreateRequestDTO request) {
        // MVP rule: initial password is derived from birthDate using ddMMyyyy.
        return request.birthDate().format(INITIAL_PASSWORD_FORMAT);
    }
}
