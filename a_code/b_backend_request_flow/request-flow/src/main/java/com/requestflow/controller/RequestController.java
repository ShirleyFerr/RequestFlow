package com.requestflow.controller;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.dto.ai.AiSuggestionDTO;
import com.requestflow.dto.ai.AiSuggestionRequestDTO;
import com.requestflow.dto.comment.CommentCreateDTO;
import com.requestflow.dto.common.PageResponseDTO;
import com.requestflow.dto.request.ReassignRequestDTO;
import com.requestflow.dto.request.RequestCreateDTO;
import com.requestflow.dto.request.RequestResponseDTO;
import com.requestflow.dto.request.RequestSummaryDTO;
import com.requestflow.dto.request.RequestUpdateStatusDTO;
import com.requestflow.integration.AiService;
import com.requestflow.service.RequestService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/requests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RequestController {

    @Inject
    RequestService requestService;

    @Inject
    AiService aiService;

    @POST
    public Response create(@Valid RequestCreateDTO requestCreateDTO) {
        RequestResponseDTO createdRequest = requestService.create(requestCreateDTO);
        return Response.created(URI.create("/requests/" + createdRequest.id()))
                .entity(createdRequest)
                .build();
    }

    @POST
    @Path("/ai-suggestion")
    public AiSuggestionDTO suggestWithAi(@Valid AiSuggestionRequestDTO suggestionRequest) {
        return aiService.suggest(suggestionRequest);
    }

    @PUT
    @Path("/{id}/reassign")
    @RolesAllowed("MANAGER")
    public RequestResponseDTO reassign(@PathParam("id") Long id, @Valid ReassignRequestDTO reassignRequest) {
        return requestService.reassign(id, reassignRequest);
    }

    @GET
    @Path("/{id}")
    public RequestResponseDTO findById(@PathParam("id") Long id) {
        return requestService.findById(id);
    }

    @POST
    @Path("/{id}/comments")
    public RequestResponseDTO addComment(@PathParam("id") Long id, @Valid CommentCreateDTO commentCreateDTO) {
        return requestService.addComment(id, commentCreateDTO);
    }

    @PUT
    @Path("/{id}/assume")
    @RolesAllowed("ANALYST")
    public RequestResponseDTO assume(@PathParam("id") Long id) {
        return requestService.assume(id);
    }

    @PUT
    @Path("/{id}/status")
    @RolesAllowed({"ANALYST", "MANAGER"})
    public RequestResponseDTO updateStatus(
            @PathParam("id") Long id,
            @Valid RequestUpdateStatusDTO updateStatusDTO
    ) {
        return requestService.updateStatus(id, updateStatusDTO);
    }

    @PUT
    @Path("/{id}/resolve")
    @RolesAllowed({"ANALYST", "MANAGER"})
    public RequestResponseDTO resolve(@PathParam("id") Long id, RequestUpdateStatusDTO updateStatusDTO) {
        return requestService.resolve(id, updateStatusDTO == null ? null : updateStatusDTO.note());
    }

    @PUT
    @Path("/{id}/cancel")
    @RolesAllowed({"ANALYST", "MANAGER"})
    public RequestResponseDTO cancel(@PathParam("id") Long id, RequestUpdateStatusDTO updateStatusDTO) {
        return requestService.cancel(id, updateStatusDTO == null ? null : updateStatusDTO.note());
    }

    @GET
    public PageResponseDTO<RequestSummaryDTO> list(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("sort") String sort,
            @QueryParam("status") RequestStatus status,
            @QueryParam("priority") RequestPriority priority,
            @QueryParam("category") RequestCategory category,
            @QueryParam("assigneeId") Long assigneeId,
            @QueryParam("requesterId") Long requesterId,
            @QueryParam("overdue") Boolean overdue
    ) {
        return requestService.list(page, size, sort, status, priority, category, assigneeId, requesterId, overdue);
    }
}
