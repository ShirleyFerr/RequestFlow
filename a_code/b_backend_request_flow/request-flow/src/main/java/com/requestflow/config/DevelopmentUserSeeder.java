package com.requestflow.config;

import com.requestflow.domain.entity.Request;
import com.requestflow.domain.entity.RequestComment;
import com.requestflow.domain.entity.StatusHistory;
import com.requestflow.domain.entity.User;
import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.RequestStatus;
import com.requestflow.domain.enums.Role;
import com.requestflow.repository.RequestRepository;
import com.requestflow.repository.UserRepository;
import com.requestflow.service.PasswordService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
public class DevelopmentUserSeeder {

    private static final String INITIAL_PASSWORD = "123456";

    @Inject
    UserRepository userRepository;

    @Inject
    RequestRepository requestRepository;

    @Inject
    PasswordService passwordService;

    @ConfigProperty(name = "requestflow.seed.enabled", defaultValue = "false")
    boolean seedEnabled;

    @Transactional
    void seed(@Observes StartupEvent event) {
        if (!seedEnabled) {
            return;
        }

        User requester = createUserIfMissing("Usuario RequestFlow", "user@requestflow.com", Role.USER, LocalDate.of(1995, 5, 10));
        User analyst = createUserIfMissing("Analista RequestFlow", "analyst@requestflow.com", Role.ANALYST, LocalDate.of(1990, 8, 15));
        User manager = createUserIfMissing("Gestor RequestFlow", "manager@requestflow.com", Role.MANAGER, LocalDate.of(1988, 3, 20));
        User analystAlice = createUserIfMissing("Alice Moura", "alice@requestflow.com", Role.ANALYST, LocalDate.of(1992, 11, 4));
        User analystCarlos = createUserIfMissing("Carlos Melo", "carlos@requestflow.com", Role.ANALYST, LocalDate.of(1989, 7, 22));

        seedRequests(requester, analyst, manager, analystAlice, analystCarlos);
    }

    private User createUserIfMissing(String name, String email, Role role, LocalDate birthDate) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.getBirthDate() == null) {
                        existingUser.setBirthDate(birthDate);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPasswordHash(passwordService.hash(INITIAL_PASSWORD));
                    user.setBirthDate(birthDate);
                    user.setRole(role);
                    user.setActive(true);
                    userRepository.persist(user);
                    return user;
                });
    }

    private void seedRequests(User requester, User analyst, User manager, User analystAlice, User analystCarlos) {
        LocalDateTime now = LocalDateTime.now();

        createRequestIfMissing(
                "RF-SEED-001 - Acesso ao sistema financeiro",
                "Nao consigo acessar o sistema financeiro com meu login corporativo.",
                RequestCategory.ACCESS,
                RequestPriority.HIGH,
                RequestStatus.OPEN,
                requester,
                null,
                now.minusDays(1),
                now.minusHours(6),
                now.plusHours(4),
                null,
                "Solicitacao de acesso pendente para fila sem responsavel.",
                "Preciso acessar hoje para fechar as pendencias do setor.",
                null
        );

        createRequestIfMissing(
                "RF-SEED-002 - Bug no portal interno",
                "Erro ao salvar formulario de solicitacao no portal interno.",
                RequestCategory.BUG,
                RequestPriority.CRITICAL,
                RequestStatus.IN_PROGRESS,
                requester,
                analyst,
                now.minusDays(3),
                now.minusHours(2),
                now.minusHours(8),
                null,
                "Bug critico em atendimento pelo analista principal.",
                "Erro ocorre sempre apos clicar em salvar.",
                "Analise iniciada pelo time de suporte."
        );

        createRequestIfMissing(
                "RF-SEED-003 - Aguardando informacoes do solicitante",
                "Ajuste em perfil de acesso requer confirmacao do gestor da area.",
                RequestCategory.SUPPORT,
                RequestPriority.MEDIUM,
                RequestStatus.WAITING_INFO,
                requester,
                analyst,
                now.minusDays(2),
                now.minusHours(10),
                now.plusDays(1),
                null,
                "Solicitacao aguardando dados adicionais.",
                "Enviei os dados solicitados parcialmente.",
                "Aguardando informacoes complementares."
        );

        createRequestIfMissing(
                "RF-SEED-004 - Funcionalidade de relatorios mensais",
                "Solicito melhoria para gerar relatorios mensais por departamento.",
                RequestCategory.REQUEST,
                RequestPriority.LOW,
                RequestStatus.RESOLVED,
                requester,
                analystAlice,
                now.minusDays(15),
                now.minusDays(7),
                now.minusDays(3),
                now.minusDays(7),
                "Melhoria atendida para relatorios mensais.",
                "Obrigada, ficou conforme esperado.",
                "Solicitacao resolvida e validada."
        );

        createRequestIfMissing(
                "RF-SEED-005 - Incidente em producao",
                "Operacao parada por indisponibilidade em producao.",
                RequestCategory.INCIDENT,
                RequestPriority.CRITICAL,
                RequestStatus.OPEN,
                requester,
                null,
                now.minusHours(3),
                now.minusHours(1),
                now.minusHours(1),
                null,
                "Incidente critico vencido e sem responsavel.",
                "Impacto alto na operacao.",
                null
        );

        createRequestIfMissing(
                "RF-SEED-006 - Solicitacao cancelada",
                "Pedido duplicado de liberacao de equipamento.",
                RequestCategory.OTHER,
                RequestPriority.MEDIUM,
                RequestStatus.CANCELLED,
                requester,
                analystCarlos,
                now.minusDays(8),
                now.minusDays(6),
                now.minusDays(5),
                null,
                "Solicitacao cancelada por duplicidade.",
                "Identifiquei que ja havia outro chamado aberto.",
                "Cancelada por duplicidade."
        );

        createRequestIfMissing(
                "RF-SEED-007 - Suporte para configuracao de impressora",
                "Preciso de ajuda para configurar impressora do setor.",
                RequestCategory.SUPPORT,
                RequestPriority.LOW,
                RequestStatus.OPEN,
                manager,
                analystCarlos,
                now.minusHours(8),
                now.minusHours(3),
                now.plusDays(3),
                null,
                "Suporte simples dentro do prazo.",
                "Equipamento esta instalado na sala 402.",
                null
        );

        createRequestIfMissing(
                "RF-SEED-008 - Falha intermitente no VPN",
                "VPN apresenta falha intermitente e desconecta durante reunioes.",
                RequestCategory.BUG,
                RequestPriority.HIGH,
                RequestStatus.IN_PROGRESS,
                requester,
                analystAlice,
                now.minusDays(5),
                now.minusDays(1),
                now.plusHours(12),
                null,
                "Falha de VPN proxima do vencimento.",
                "A queda ocorre duas ou tres vezes por dia.",
                "Investigacao tecnica em andamento."
        );
    }

    private void createRequestIfMissing(
            String title,
            String description,
            RequestCategory category,
            RequestPriority priority,
            RequestStatus status,
            User requester,
            User assignee,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime dueDate,
            LocalDateTime resolvedAt,
            String aiSummary,
            String initialComment,
            String historyNote
    ) {
        if (requestRepository.find("title", title).firstResultOptional().isPresent()) {
            return;
        }

        Request request = new Request();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setPriority(priority);
        request.setStatus(status);
        request.setRequester(requester);
        request.setAssignee(assignee);
        request.setCreatedAt(createdAt);
        request.setUpdatedAt(updatedAt);
        request.setDueDate(dueDate);
        request.setResolvedAt(resolvedAt);
        request.setAiSummary(aiSummary);

        addHistory(request, null, RequestStatus.OPEN, requester, "Solicitacao criada", createdAt);
        if (status != RequestStatus.OPEN) {
            addHistory(request, RequestStatus.OPEN, status, assignee != null ? assignee : requester, historyNote, updatedAt);
        }

        if (initialComment != null && !initialComment.isBlank()) {
            addComment(request, requester, initialComment, createdAt.plusMinutes(20));
        }

        requestRepository.persist(request);
    }

    private void addComment(Request request, User author, String message, LocalDateTime createdAt) {
        RequestComment comment = new RequestComment();
        comment.setRequest(request);
        comment.setAuthor(author);
        comment.setMessage(message);
        comment.setCreatedAt(createdAt);
        request.getComments().add(comment);
    }

    private void addHistory(
            Request request,
            RequestStatus oldStatus,
            RequestStatus newStatus,
            User changedBy,
            String note,
            LocalDateTime changedAt
    ) {
        StatusHistory history = new StatusHistory();
        history.setRequest(request);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        history.setChangedAt(changedAt);
        request.getStatusHistory().add(history);
    }
}
