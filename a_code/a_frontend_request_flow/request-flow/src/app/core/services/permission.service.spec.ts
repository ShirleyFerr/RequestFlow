import { TestBed } from '@angular/core/testing';
import { MOCK_REQUESTS, MOCK_USERS } from '../mocks';
import { RequestStatus, Role } from '../models';
import { PermissionService } from './permission.service';

describe('PermissionService', () => {
  let service: PermissionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PermissionService);
  });

  it('should return default routes by role', () => {
    expect(service.getDefaultRouteByRole(Role.USER)).toBe('/requests');
    expect(service.getDefaultRouteByRole(Role.ANALYST)).toBe('/dashboard');
    expect(service.getDefaultRouteByRole(Role.MANAGER)).toBe('/dashboard');
  });

  it('should expose menu and route permissions by role', () => {
    expect(service.canAccessDashboard(Role.USER)).toBe(false);
    expect(service.canCreateRequest(Role.USER)).toBe(true);
    expect(service.canCreateRequest(Role.ANALYST)).toBe(false);
    expect(service.canCreateRequest(Role.MANAGER)).toBe(false);
    expect(service.canAccessUsers(Role.MANAGER)).toBe(true);
    expect(service.canAccessUsers(Role.ANALYST)).toBe(false);
  });

  it('should scope request visibility by persona', () => {
    const [user, analyst, manager] = MOCK_USERS;

    expect(MOCK_REQUESTS.every((request) => service.canViewRequest(Role.MANAGER, request, manager))).toBe(true);
    expect(
      MOCK_REQUESTS
        .filter((request) => service.canViewRequest(Role.USER, request, user))
        .every((request) => request.requester.id === user.id),
    ).toBe(true);
    expect(
      MOCK_REQUESTS
        .filter((request) => service.canViewRequest(Role.ANALYST, request, analyst))
        .every(
          (request) =>
            request.assignedTo?.id === analyst.id ||
            (!request.assignedTo && ![RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)),
        ),
    ).toBe(true);
  });

  it('should centralize action permissions', () => {
    const openRequest = MOCK_REQUESTS.find((request) => request.status === RequestStatus.OPEN && !request.assignedTo)!;
    const cancelledRequest = MOCK_REQUESTS.find((request) => request.status === RequestStatus.CANCELLED)!;

    expect(service.canAssumeRequest(Role.ANALYST, openRequest)).toBe(true);
    expect(service.canChangeStatus(Role.USER, openRequest)).toBe(false);
    expect(service.canChangeStatus(Role.ANALYST, openRequest)).toBe(false);
    expect(service.canChangeStatus(Role.ANALYST, { ...openRequest, assignedTo: MOCK_USERS[1] })).toBe(true);
    expect(service.canReassignRequest(Role.MANAGER, openRequest)).toBe(true);
    expect(service.canComment(Role.MANAGER, cancelledRequest)).toBe(false);
  });
});
