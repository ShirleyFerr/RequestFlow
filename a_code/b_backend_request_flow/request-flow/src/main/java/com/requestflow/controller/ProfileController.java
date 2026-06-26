package com.requestflow.controller;

import com.requestflow.dto.profile.ChangePasswordDTO;
import com.requestflow.dto.profile.ProfileResponseDTO;
import com.requestflow.service.ProfileService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/profile")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileController {

    @Inject
    ProfileService profileService;

    @GET
    public ProfileResponseDTO getProfile() {
        return profileService.getProfile();
    }

    @PUT
    @Path("/password")
    public ProfileResponseDTO changePassword(@Valid ChangePasswordDTO request) {
        return profileService.changePassword(request);
    }
}
