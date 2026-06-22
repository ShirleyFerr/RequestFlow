package com.requestflow.controller;

import com.requestflow.dto.auth.LoginRequestDTO;
import com.requestflow.dto.auth.LoginResponseDTO;
import com.requestflow.dto.user.UserResponseDTO;
import com.requestflow.service.AuthService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    public LoginResponseDTO login(@Valid LoginRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }

    @GET
    @Path("/me")
    @Authenticated
    public UserResponseDTO me() {
        return authService.me();
    }
}
