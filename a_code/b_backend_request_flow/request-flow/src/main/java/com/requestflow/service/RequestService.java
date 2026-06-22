package com.requestflow.service;

import com.requestflow.domain.entity.Request;
import com.requestflow.domain.entity.RequestComment;
import com.requestflow.domain.entity.StatusHistory;
import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.Role;
import com.requestflow.dto.comment.CommentCreateDTO;
import com.requestflow.dto.common.PageResponseDTO;
import com.requestflow.dto.request.ReassignRequestDTO;
import com.requestflow.dto.request.RequestCreateDTO;
import com.requestflow.dto.request.RequestResponseDTO;
import com.requestflow.dto.request.RequestSummaryDTO;
import com.requestflow.dto.request.RequestUpdateStatusDTO;
import com.requestflow.exception.BusinessException;
import com.requestflow.exception.ForbiddenException;
import com.requestflow.exception.NotFoundException;
import com.requestflow.mapper.RequestMapper;
import com.requestflow.repository.RequestCommentRepository;
import com.requestflow.repository.RequestRepository;
import com.requestflow.repository.StatusHistoryRepository;
import com.requestflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class RequestService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    @Inject
    RequestRepository requestRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    StatusHistoryRepository statusHistoryRepository;

    @Inject
    RequestCommentRepository requestCommentRepository;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    RequestMapper requestMapper;

    public RequestResponseDTO findById(Long requestId) {
        User currentUser = currentUserService.getCurrentUser();
        Request request = findRequestOrThrow(requestId);
        ensureCanView(currentUser, request);
        return requestMapper.toResponse(request);
    }

    @Transactional
    public RequestResponseDTO addComment(Long requestId, CommentCreateDTO commentCreateDTO) {
        User currentUser = currentUserService.getCurrentUser();
        Request request = findRequestOrThrow(requestId);
        ensureCanView(currentUser, request);
        ensureNotCancelled(request);

        RequestComment comment = new RequestComment();
        comment.setRequest(request);
        comment.setAuthor(currentUser);
        comment.setMessage(commentCreateDTO.message().trim());

        request.getComments().add(comment);
        request.setUpdatedAt(LocalDateTime.now());
        requestCommentRepository.persist(comment);

        return requestMapper.toResponse(request);
    }

    @Transactional
    public RequestResponseDTO assume(Long requestId) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ANALYST) {
            throw new ForbiddenException("Somente analistas podem assumir solicitacoes");
        }

        Request request = findRequestOrThrow(requestId);
        ensureNotCancelled(request);

        if (request.getAssignee() != null) {
            throw new BusinessException("Solicitacao ja possui responsavel");
        }

        request.setAssignee(currentUser);
        request.setUpdatedAt(LocalDateTime.now());
        addHistory(request, request.getStatus(), request.getStatus(), currentUser,
                "Solicitacao assumida por " + currentUser.getName());

        return requestMapper.toResponse(request);
    }

    @Transactional
    public RequestResponseDTO updateStatus(Long requestId, RequestUpdateStatusDTO updateStatusDTO) {
        User currentUser = currentUserService.getCurrentUser();
        Request request = findRequestOrThrow(requestId);
        ensureCanChangeStatus(currentUser, request);
        ensureNotCancelled(request);

        RequestStatus oldStatus = request.getStatus();
        RequestStatus newStatus = updateStatusDTO.newStatus();

        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());
        request.setResolvedAt(newStatus == RequestStatus.RESOLVED ? LocalDateTime.now() : null);

        addHistory(request, oldStatus, newStatus, currentUser, updateStatusDTO.note());

        return requestMapper.toResponse(request);
    }

    @Transactional
    public RequestResponseDTO resolve(Long requestId, String note) {
        return updateStatus(requestId, new RequestUpdateStatusDTO(RequestStatus.RESOLVED, note));
    }

    @Transactional
    public RequestResponseDTO cancel(Long requestId, String note) {
        return updateStatus(requestId, new RequestUpdateStatusDTO(RequestStatus.CANCELLED, note));
    }

    @Transactional
    public RequestResponseDTO reassign(Long requestId, ReassignRequestDTO reassignRequest) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Somente gestores podem reatribuir responsavel");
        }

        Request request = findRequestOrThrow(requestId);

        ensureNotCancelled(request);

        User newAssignee = userRepository.findByIdOptional(reassignRequest.assigneeId())
                .orElseThrow(() -> new NotFoundException("Novo responsavel nao encontrado"));

        if (!Boolean.TRUE.equals(newAssignee.getActive()) || newAssignee.getRole() != Role.ANALYST) {
            throw new BusinessException("Novo responsavel deve ser um analista ativo");
        }

        request.setAssignee(newAssignee);
        request.setUpdatedAt(LocalDateTime.now());

        addHistory(request, request.getStatus(), request.getStatus(), currentUser,
                buildReassignNote(newAssignee, reassignRequest.note()));

        return requestMapper.toResponse(request);
    }

    @Transactional
    public RequestResponseDTO create(RequestCreateDTO requestCreateDTO) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.USER) {
            throw new ForbiddenException("Somente solicitantes podem criar solicitacoes");
        }

        Request request = new Request();
        request.setTitle(requestCreateDTO.title());
        request.setDescription(requestCreateDTO.description());
        request.setCategory(requestCreateDTO.category());
        request.setPriority(requestCreateDTO.priority());
        request.setStatus(RequestStatus.OPEN);
        request.setRequester(currentUser);
        request.setAssignee(null);
        request.setDueDate(requestCreateDTO.dueDate());
        request.setResolvedAt(null);
        request.setAiSummary(requestCreateDTO.aiSummary());

        StatusHistory initialHistory = new StatusHistory();
        initialHistory.setRequest(request);
        initialHistory.setOldStatus(null);
        initialHistory.setNewStatus(RequestStatus.OPEN);
        initialHistory.setChangedBy(currentUser);
        initialHistory.setNote("Solicitação criada");
        request.getStatusHistory().add(initialHistory);

        requestRepository.persist(request);

        return requestMapper.toResponse(request);
    }

    public PageResponseDTO<RequestSummaryDTO> list(
            Integer page,
            Integer size,
            String sort,
            RequestStatus status,
            RequestPriority priority,
            RequestCategory category,
            Long assigneeId,
            Long requesterId,
            Boolean overdue
    ) {
        User currentUser = currentUserService.getCurrentUser();
        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);

        RequestRepository.RequestPage result = requestRepository.findVisibleRequests(
                currentUser,
                status,
                priority,
                category,
                assigneeId,
                requesterId,
                overdue,
                resolvedPage,
                resolvedSize,
                sort
        );

        List<RequestSummaryDTO> content = result.content().stream()
                .map(requestMapper::toSummary)
                .toList();

        int totalPages = calculateTotalPages(result.totalElements(), resolvedSize);

        return new PageResponseDTO<>(
                content,
                resolvedPage,
                resolvedSize,
                result.totalElements(),
                totalPages
        );
    }

    private int resolvePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int resolveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }

    private String buildReassignNote(User newAssignee, String note) {
        String baseNote = "Responsavel reatribuido para " + newAssignee.getName();
        if (note == null || note.isBlank()) {
            return baseNote;
        }
        return baseNote + ". Observacao: " + note.trim();
    }

    private Request findRequestOrThrow(Long requestId) {
        return requestRepository.findByIdOptional(requestId)
                .orElseThrow(() -> new NotFoundException("Solicitacao nao encontrada"));
    }

    private void ensureCanView(User currentUser, Request request) {
        Role role = currentUser.getRole();

        if (role == Role.MANAGER) {
            return;
        }

        if (role == Role.USER && request.getRequester().getId().equals(currentUser.getId())) {
            return;
        }

        if (role == Role.ANALYST
                && (request.getAssignee() == null || request.getAssignee().getId().equals(currentUser.getId()))) {
            return;
        }

        throw new ForbiddenException("Usuario nao possui permissao para acessar esta solicitacao");
    }

    private void ensureCanChangeStatus(User currentUser, Request request) {
        if (currentUser.getRole() == Role.MANAGER) {
            return;
        }

        if (currentUser.getRole() == Role.ANALYST
                && request.getAssignee() != null
                && request.getAssignee().getId().equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException("Usuario nao possui permissao para alterar status desta solicitacao");
    }

    private void ensureNotCancelled(Request request) {
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException("Solicitacao cancelada e somente leitura");
        }
    }

    private void addHistory(
            Request request,
            RequestStatus oldStatus,
            RequestStatus newStatus,
            User changedBy,
            String note
    ) {
        StatusHistory history = new StatusHistory();
        history.setRequest(request);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);

        request.getStatusHistory().add(history);
        statusHistoryRepository.persist(history);
    }
}
