package com.requestflow.controller;

import com.requestflow.domain.enums.Role;
import com.requestflow.dto.user.UserActiveUpdateDTO;
import com.requestflow.dto.user.UserCreateRequestDTO;
import com.requestflow.dto.user.UserResponseDTO;
import com.requestflow.dto.user.UserSummaryDTO;
import com.requestflow.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("MANAGER")
public class UserController {

    @Inject
    UserService userService;

    @GET
    public List<UserResponseDTO> list(@QueryParam("role") Role role, @QueryParam("active") Boolean active) {
        return userService.list(role, active);
    }

    @GET
    @Path("/analysts")
    public List<UserSummaryDTO> listActiveAnalysts() {
        return userService.listActiveAnalysts();
    }

    @GET
    @Path("/{id}")
    public UserResponseDTO findById(@PathParam("id") Long id) {
        return userService.findById(id);
    }

    @POST
    public Response create(@Valid UserCreateRequestDTO request) {
        UserResponseDTO createdUser = userService.create(request);
        return Response.created(URI.create("/users/" + createdUser.id()))
                .entity(createdUser)
                .build();
    }

    @PATCH
    @Path("/{id}/active")
    public UserResponseDTO updateActive(@PathParam("id") Long id, @Valid UserActiveUpdateDTO request) {
        return userService.updateActive(id, request);
    }
}
