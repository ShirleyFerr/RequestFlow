import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AISuggestion,
  Pagination,
  Request,
  RequestCategory,
  RequestPriority,
  RequestStatus,
  Role,
  StatusHistory,
  User,
} from '../models';

export interface RequestFilterAvailability {
  search: boolean;
  status: boolean;
  priority: boolean;
  category: boolean;
  responsible: boolean;
  requester: boolean;
}

export interface RequestQuery {
  page?: number;
  size?: number;
  sort?: string;
  status?: RequestStatus | '';
  priority?: RequestPriority | '';
  category?: RequestCategory | '';
  assigneeId?: string;
  requesterId?: string;
  overdue?: boolean;
}

export interface RequestCreatePayload {
  title: string;
  description: string;
  category: RequestCategory;
  priority: RequestPriority;
  dueDate: string;
  aiSummary?: string;
}

interface ApiPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface ApiUser {
  id: number | string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt?: string;
}

interface ApiComment {
  id: number | string;
  author: ApiUser;
  message: string;
  createdAt: string;
}

interface ApiHistory {
  id: number | string;
  oldStatus: RequestStatus | null;
  newStatus: RequestStatus;
  changedBy: ApiUser;
  changedAt: string;
  note?: string;
}

interface ApiRequest {
  id: number | string;
  title: string;
  description?: string;
  category: RequestCategory;
  priority: RequestPriority;
  status: RequestStatus;
  requester: ApiUser;
  assignee?: ApiUser | null;
  createdAt: string;
  updatedAt?: string;
  dueDate: string;
  resolvedAt?: string | null;
  aiSummary?: string | null;
  comments?: ApiComment[];
  statusHistory?: ApiHistory[];
  slaStatus?: Request['slaStatus'];
  overdue?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class RequestService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listRequests(query: RequestQuery = {}): Observable<Pagination<Request>> {
    return this.http
      .get<ApiPage<ApiRequest>>(`${this.apiUrl}/requests`, {
        params: this.buildParams(query),
      })
      .pipe(
        map((page) => ({
          items: page.content.map((request) => this.mapApiRequest(request)),
          pageIndex: page.page,
          pageSize: page.size,
          totalItems: page.totalElements,
          totalPages: page.totalPages,
        })),
      );
  }

  getRequestsByRole(_role: Role, _currentUser: User): Observable<Request[]> {
    return this.listRequests({ size: 100 }).pipe(map((page) => page.items));
  }

  getAvailableFiltersByRole(role: Role): RequestFilterAvailability {
    return {
      search: role !== Role.USER,
      status: true,
      priority: role === Role.ANALYST || role === Role.MANAGER,
      category: role === Role.ANALYST || role === Role.MANAGER,
      responsible: role === Role.MANAGER,
      requester: role === Role.MANAGER,
    };
  }

  getRequestById(requestId: string): Observable<Request> {
    return this.http
      .get<ApiRequest>(`${this.apiUrl}/requests/${requestId}`)
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  createRequest(payload: RequestCreatePayload): Observable<Request> {
    return this.http
      .post<ApiRequest>(`${this.apiUrl}/requests`, payload)
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  addComment(requestId: string, message: string): Observable<Request> {
    return this.http
      .post<ApiRequest>(`${this.apiUrl}/requests/${requestId}/comments`, { message })
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  assumeRequest(requestId: string): Observable<Request> {
    return this.http
      .put<ApiRequest>(`${this.apiUrl}/requests/${requestId}/assume`, {})
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  updateStatus(requestId: string, status: RequestStatus, note?: string): Observable<Request> {
    return this.http
      .put<ApiRequest>(`${this.apiUrl}/requests/${requestId}/status`, {
        newStatus: status,
        note,
      })
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  resolveRequest(requestId: string, note?: string): Observable<Request> {
    return this.http
      .put<ApiRequest>(`${this.apiUrl}/requests/${requestId}/resolve`, { note })
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  cancelRequest(requestId: string, note?: string): Observable<Request> {
    return this.http
      .put<ApiRequest>(`${this.apiUrl}/requests/${requestId}/cancel`, { note })
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  reassignRequest(requestId: string, analystId: string, note?: string): Observable<Request> {
    return this.http
      .put<ApiRequest>(`${this.apiUrl}/requests/${requestId}/reassign`, {
        assigneeId: Number(analystId),
        note,
      })
      .pipe(map((request) => this.mapApiRequest(request)));
  }

  getActiveAnalysts(): Observable<User[]> {
    return this.http
      .get<ApiUser[]>(`${this.apiUrl}/users/analysts`)
      .pipe(map((users) => users.map((user) => this.mapUser(user))));
  }

  private buildParams(query: RequestQuery): HttpParams {
    let params = new HttpParams();

    Object.entries(query).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }

      params = params.set(key, String(value));
    });

    return params;
  }

  mapApiRequest(request: ApiRequest): Request {
    const id = String(request.id);
    const aiSuggestion: AISuggestion | undefined = request.aiSummary
      ? {
          category: request.category,
          priority: request.priority,
          summary: request.aiSummary,
        }
      : undefined;

    return {
      id,
      title: request.title,
      description: request.description ?? '',
      summary: request.aiSummary ?? request.description ?? request.title,
      category: request.category,
      priority: request.priority,
      status: request.status,
      requester: this.mapUser(request.requester),
      assignedTo: request.assignee ? this.mapUser(request.assignee) : undefined,
      dueDate: request.dueDate,
      createdAt: request.createdAt,
      updatedAt: request.updatedAt ?? request.createdAt,
      resolvedAt: request.resolvedAt ?? undefined,
      aiSuggestion,
      comments: (request.comments ?? []).map((comment) => ({
        id: String(comment.id),
        requestId: id,
        author: this.mapUser(comment.author),
        message: comment.message,
        createdAt: comment.createdAt,
      })),
      statusHistory: (request.statusHistory ?? []).map((history) => this.mapHistory(history, id)),
      slaStatus: request.slaStatus,
      overdue: request.overdue,
    };
  }

  private mapHistory(history: ApiHistory, requestId: string): StatusHistory {
    return {
      id: String(history.id),
      requestId,
      fromStatus: history.oldStatus,
      toStatus: history.newStatus,
      changedBy: this.mapUser(history.changedBy),
      changedAt: history.changedAt,
      note: history.note,
    };
  }

  private mapUser(user: ApiUser): User {
    return {
      id: String(user.id),
      name: user.name,
      email: user.email,
      role: user.role,
      active: user.active,
      createdAt: user.createdAt,
    };
  }
}
