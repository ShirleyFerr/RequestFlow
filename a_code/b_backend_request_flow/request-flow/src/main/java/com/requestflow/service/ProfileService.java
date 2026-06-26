package com.requestflow.service;

import com.requestflow.domain.entity.User;
import com.requestflow.dto.profile.ChangePasswordDTO;
import com.requestflow.dto.profile.ProfileResponseDTO;
import com.requestflow.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProfileService {

    @Inject
    CurrentUserService currentUserService;

    @Inject
    PasswordService passwordService;

    public ProfileResponseDTO getProfile() {
        return toResponse(currentUserService.getCurrentUser());
    }

    @Transactional
    public ProfileResponseDTO changePassword(ChangePasswordDTO request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Nova senha e confirmacao devem ser iguais");
        }

        User currentUser = currentUserService.getCurrentUser();
        currentUser.setPasswordHash(passwordService.hash(request.newPassword()));

        return toResponse(currentUser);
    }

    private ProfileResponseDTO toResponse(User user) {
        return new ProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getBirthDate()
        );
    }
}
