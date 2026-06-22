import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import {
  AnalystDashboard,
  CurrentUserService,
  DashboardService,
  ManagerDashboard,
} from '../../../core/services';
import {
  Request as RequestFlowRequest,
  RequestPriority,
  RequestStatus,
  Role,
} from '../../../core/models';
import { EmptyStateComponent, PageHeaderComponent } from '../../../shared/components';
import { priorityLabel, statusLabel } from '../../../shared/pipes';
import { ManagerDashboardComponent } from '../manager-dashboard/manager-dashboard';

interface DashboardMetric {
  label: string;
  value: number;
  icon: string;
  tone: 'default' | 'warning' | 'success' | 'danger';
}

@Component({
  selector: 'app-dashboard-home',
  imports: [
    DatePipe,
    EmptyStateComponent,
    ManagerDashboardComponent,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatListModule,
    PageHeaderComponent,
  ],
  templateUrl: './dashboard-home.html',
  styleUrl: './dashboard-home.scss',
})
export class DashboardHomeComponent {
  private readonly currentUserService = inject(CurrentUserService);
  private readonly dashboardService = inject(DashboardService);
  private readonly today = this.toDateOnly(new Date());

  protected readonly role = Role;
  protected readonly priority = RequestPriority;
  protected readonly currentUser = this.currentUserService.currentUser;
  protected readonly analystDashboard = signal<AnalystDashboard | null>(null);
  protected readonly managerDashboard = signal<ManagerDashboard | null>(null);

  protected readonly metrics = computed<DashboardMetric[]>(() => {
    const dashboard = this.analystDashboard();

    return [
      {
        label: 'Atribuidas a mim',
        value: dashboard?.assignedToMe ?? 0,
        icon: 'inventory_2',
        tone: 'default',
      },
      {
        label: 'Atrasadas',
        value: dashboard?.overdueAssignedToMe ?? 0,
        icon: 'report',
        tone: 'danger',
      },
      {
        label: 'Criticas ou altas',
        value: dashboard?.highPriorityAssignedToMe ?? 0,
        icon: 'priority_high',
        tone: 'warning',
      },
      {
        label: 'Resolvidas',
        value: dashboard?.resolvedByMe ?? 0,
        icon: 'task_alt',
        tone: 'success',
      },
    ];
  });

  protected readonly analystWorkQueue = computed(() => this.analystDashboard()?.workQueue ?? []);
  protected readonly analystQueue = computed(() => this.analystDashboard()?.slaAlerts ?? []);

  constructor() {
    const user = this.currentUser();

    if (user?.role === Role.MANAGER) {
      this.dashboardService.getManagerDashboard().subscribe((dashboard) => {
        this.managerDashboard.set(dashboard);
      });
    }

    if (user?.role === Role.ANALYST) {
      this.dashboardService.getAnalystDashboard().subscribe((dashboard) => {
        this.analystDashboard.set(dashboard);
      });
    }
  }

  protected isOverdue(request: RequestFlowRequest): boolean {
    if ([RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)) {
      return false;
    }

    return this.toDateOnly(new Date(request.dueDate)) < this.today;
  }

  protected statusLabel(status: RequestStatus): string {
    return statusLabel(status);
  }

  protected priorityLabel(priority: RequestPriority): string {
    return priorityLabel(priority);
  }

  private toDateOnly(date: Date): number {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  }
}
