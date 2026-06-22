import { Injectable } from '@angular/core';
import { Request, RequestStatus, Role, User } from '../models';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  canAccessDashboard(role: Role): boolean {
    return role === Role.ANALYST || role === Role.MANAGER;
  }

  canAccessManagerDashboard(role: Role): boolean {
    return role === Role.MANAGER;
  }

  canCreateRequest(role: Role): boolean {
    return role === Role.USER;
  }

  canAccessUsers(role: Role): boolean {
    return role === Role.MANAGER;
  }

  canAssumeRequest(role: Role, request: Request): boolean {
    return (
      role === Role.ANALYST &&
      !request.assignedTo &&
      ![RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)
    );
  }

  canChangeStatus(role: Role, request: Request): boolean {
    if (request.status === RequestStatus.CANCELLED) {
      return false;
    }

    if (role === Role.MANAGER) {
      return true;
    }

    return role === Role.ANALYST && !!request.assignedTo;
  }

  canResolveRequest(role: Role, request: Request): boolean {
    return this.canChangeStatus(role, request);
  }

  canCancelRequest(role: Role, request: Request): boolean {
    return this.canChangeStatus(role, request);
  }

  canReassignRequest(role: Role, request: Request): boolean {
    return role === Role.MANAGER && request.status !== RequestStatus.CANCELLED;
  }

  canComment(role: Role, request: Request): boolean {
    return request.status !== RequestStatus.CANCELLED && [Role.USER, Role.ANALYST, Role.MANAGER].includes(role);
  }

  canViewRequest(role: Role, request: Request, currentUser: User): boolean {
    if (role === Role.MANAGER) {
      return true;
    }

    if (role === Role.ANALYST) {
      return (
        request.assignedTo?.id === currentUser.id ||
        (!request.assignedTo && ![RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status))
      );
    }

    return request.requester.id === currentUser.id;
  }

  getDefaultRouteByRole(role: Role): string {
    return role === Role.USER ? '/requests' : '/dashboard';
  }

  getRequestsTitleByRole(role: Role): string {
    const titles: Record<Role, string> = {
      [Role.USER]: 'Minhas solicitações',
      [Role.ANALYST]: 'Solicitações abertas',
      [Role.MANAGER]: 'Todas as solicitações',
    };

    return titles[role];
  }
}
