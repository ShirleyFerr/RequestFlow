import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Request, RequestCategory, RequestPriority, RequestStatus } from '../models';
import { RequestService } from './request.service';

export interface DashboardSummary {
  totalRequests: number;
  openRequests: number;
  inProgressRequests: number;
  waitingInfoRequests: number;
  overdueRequests: number;
  resolvedRequests: number;
  resolvedThisMonthRequests: number;
  cancelledRequests: number;
  averageResolutionHours: number;
}

export interface TeamPerformance {
  userId: number;
  name: string;
  initials: string;
  assignedRequests: number;
  resolvedThisMonthRequests: number;
  overdueRequests: number;
  averageResolutionHours: number;
}

export interface AnalystDashboard {
  summary: DashboardSummary;
  assignedToMe: number;
  overdueAssignedToMe: number;
  highPriorityAssignedToMe: number;
  resolvedByMe: number;
  averageResolutionHours: number;
  workQueue: Request[];
  slaAlerts: Request[];
  criticalOrHighPriority: Request[];
}

interface ApiAnalystDashboard extends Omit<AnalystDashboard, 'workQueue' | 'slaAlerts' | 'criticalOrHighPriority'> {
  workQueue: unknown[];
  slaAlerts: unknown[];
  criticalOrHighPriority: unknown[];
}

export interface ManagerDashboard {
  summary: DashboardSummary;
  byStatus: Partial<Record<RequestStatus, number>>;
  byPriority: Partial<Record<RequestPriority, number>>;
  byCategory: Partial<Record<RequestCategory, number>>;
  teamPerformance: TeamPerformance[];
  slaAlerts: Request[];
  criticalOpenRequests: Request[];
  unassignedRequests: Request[];
}

interface ApiManagerDashboard extends Omit<ManagerDashboard, 'slaAlerts' | 'criticalOpenRequests' | 'unassignedRequests'> {
  slaAlerts: unknown[];
  criticalOpenRequests: unknown[];
  unassignedRequests: unknown[];
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly requestService = inject(RequestService);
  private readonly apiUrl = environment.apiUrl;

  getAnalystDashboard(): Observable<AnalystDashboard> {
    return this.http.get<ApiAnalystDashboard>(`${this.apiUrl}/dashboard/analyst`).pipe(
      map((dashboard) => ({
        ...dashboard,
        workQueue: this.mapSummaryList(dashboard.workQueue),
        slaAlerts: this.mapSummaryList(dashboard.slaAlerts),
        criticalOrHighPriority: this.mapSummaryList(dashboard.criticalOrHighPriority),
      })),
    );
  }

  getManagerDashboard(): Observable<ManagerDashboard> {
    return this.http.get<ApiManagerDashboard>(`${this.apiUrl}/dashboard/manager`).pipe(
      map((dashboard) => ({
        ...dashboard,
        slaAlerts: this.mapSummaryList(dashboard.slaAlerts),
        criticalOpenRequests: this.mapSummaryList(dashboard.criticalOpenRequests),
        unassignedRequests: this.mapSummaryList(dashboard.unassignedRequests),
      })),
    );
  }

  private mapSummaryList(items: unknown[]): Request[] {
    return items.map((item) => this.requestService.mapApiRequest(item as never));
  }
}
