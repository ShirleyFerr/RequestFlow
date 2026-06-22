import { MOCK_REQUESTS, MOCK_USERS } from '../../core/mocks';
import { Request, RequestPriority, RequestStatus, Role, User } from '../../core/models';
import { categoryLabel, priorityLabel, statusLabel } from '../../shared/pipes';

describe('request rules', () => {
  const [user, analyst, manager] = MOCK_USERS;
  const today = new Date(2026, 5, 16).getTime();

  it('should scope requests by USER profile', () => {
    const scoped = scopeRequests(user);

    expect(scoped.length).toBeGreaterThan(0);
    expect(scoped.every((request) => request.requester.id === user.id)).toBe(true);
  });

  it('should scope requests by ANALYST profile', () => {
    const scoped = scopeRequests(analyst);

    expect(scoped.length).toBeGreaterThan(0);
    expect(
      scoped.every(
        (request) =>
          request.assignedTo?.id === analyst.id ||
          (!request.assignedTo &&
            ![RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)),
      ),
    ).toBe(true);
  });

  it('should scope requests by MANAGER profile', () => {
    expect(scopeRequests(manager)).toHaveLength(MOCK_REQUESTS.length);
  });

  it('should identify overdue, near due and on track SLA states', () => {
    expect(slaState({ dueDate: '2026-06-10', status: RequestStatus.OPEN })).toBe('overdue');
    expect(slaState({ dueDate: '2026-06-17', status: RequestStatus.OPEN })).toBe('near');
    expect(slaState({ dueDate: '2026-06-24', status: RequestStatus.OPEN })).toBe('ok');
  });

  it('should keep CANCELLED requests visible but read-only', () => {
    const cancelled = MOCK_REQUESTS.find((request) => request.status === RequestStatus.CANCELLED);

    expect(cancelled).toBeTruthy();
    expect(canUpdateStatus(cancelled!, manager)).toBe(false);
    expect(canAddComment(cancelled!)).toBe(false);
    expect(scopeRequests(user).some((request) => request.id === cancelled!.id)).toBe(true);
  });

  it('should allow ANALYST to update assigned requests and MANAGER to update non-cancelled status', () => {
    const openRequest = MOCK_REQUESTS.find(
      (request) => request.status === RequestStatus.OPEN && !request.assignedTo,
    )!;
    const assignedRequest = { ...openRequest, assignedTo: analyst };

    expect(canUpdateStatus(openRequest, analyst)).toBe(false);
    expect(canUpdateStatus(assignedRequest, analyst)).toBe(true);
    expect(canUpdateStatus(openRequest, manager)).toBe(true);
    expect(canUpdateStatus(openRequest, user)).toBe(false);
  });

  it('should expose standardized labels for status, priority and category', () => {
    expect(statusLabel(RequestStatus.WAITING_INFO)).toBe('Aguardando informações');
    expect(priorityLabel(RequestPriority.CRITICAL)).toBe('Crítica');
    expect(categoryLabel(MOCK_REQUESTS[1].category)).toBe('Erro');
  });

  function scopeRequests(currentUser: User): Request[] {
    if (currentUser.role === Role.MANAGER) {
      return MOCK_REQUESTS;
    }

    if (currentUser.role === Role.ANALYST) {
      return MOCK_REQUESTS.filter(
        (request) =>
          request.assignedTo?.id === currentUser.id ||
          (!request.assignedTo &&
            ![RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)),
      );
    }

    return MOCK_REQUESTS.filter((request) => request.requester.id === currentUser.id);
  }

  function slaState(request: Pick<Request, 'dueDate' | 'status'>): 'overdue' | 'near' | 'ok' | 'closed' {
    if ([RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)) {
      return 'closed';
    }

    const dueDate = new Date(`${request.dueDate}T00:00:00`).getTime();
    const daysToDue = Math.ceil((dueDate - today) / 86_400_000);

    if (daysToDue < 0) {
      return 'overdue';
    }

    if (daysToDue <= 3) {
      return 'near';
    }

    return 'ok';
  }

  function canUpdateStatus(request: Request, currentUser: User): boolean {
    if (request.status === RequestStatus.CANCELLED) {
      return false;
    }

    if (currentUser.role === Role.MANAGER) {
      return true;
    }

    return currentUser.role === Role.ANALYST && request.assignedTo?.id === currentUser.id;
  }

  function canAddComment(request: Request): boolean {
    return request.status !== RequestStatus.CANCELLED;
  }
});
