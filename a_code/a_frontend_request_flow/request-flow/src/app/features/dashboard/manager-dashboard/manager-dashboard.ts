import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import {
  Request as RequestFlowRequest,
  RequestCategory,
  RequestStatus,
} from '../../../core/models';
import { ManagerDashboard } from '../../../core/services';
import { categoryLabel, statusLabel } from '../../../shared/pipes';

interface DistributionItem {
  label: string;
  value: number;
  shortLabel: string;
  tone: 'open' | 'progress' | 'waiting' | 'resolved';
}

interface CategoryItem {
  label: string;
  value: number;
}

interface KpiCard {
  title: string;
  value: string;
  badge: string;
  badgeTone: 'positive' | 'neutral';
  strongValue?: boolean;
}

interface TeamMember {
  initials: string;
  name: string;
  resolved: number;
}

@Component({
  selector: 'app-manager-dashboard',
  imports: [MatCardModule, MatChipsModule, MatDividerModule, MatIconModule],
  templateUrl: './manager-dashboard.html',
  styleUrl: './manager-dashboard.scss',
})
export class ManagerDashboardComponent {
  @Input() dashboard: ManagerDashboard | null = null;

  protected get kpiCards(): KpiCard[] {
    const summary = this.dashboard?.summary;

    return [
      {
        title: 'Em andamento',
        value: String(summary?.inProgressRequests ?? 0),
        badge: 'Atual',
        badgeTone: 'positive',
      },
      {
        title: 'Aguardando informacoes',
        value: String(summary?.waitingInfoRequests ?? 0),
        badge: 'Fila',
        badgeTone: 'neutral',
      },
      {
        title: 'Concluidas (mes)',
        value: String(summary?.resolvedThisMonthRequests ?? 0),
        badge: 'Resolvidas',
        badgeTone: 'positive',
      },
      {
        title: 'Tempo Medio de SLA',
        value: `${(summary?.averageResolutionHours ?? 0).toFixed(1)}h`,
        badge: 'Dentro do alvo',
        badgeTone: 'positive',
        strongValue: true,
      },
    ];
  }

  protected get teamMembers(): TeamMember[] {
    return (this.dashboard?.teamPerformance ?? []).map((member) => ({
      initials: member.initials,
      name: member.name,
      resolved: member.resolvedThisMonthRequests,
    }));
  }

  protected get statusVolume(): DistributionItem[] {
    return [
      {
        label: this.statusLabel(RequestStatus.OPEN),
        shortLabel: 'Abertas',
        value: this.dashboard?.byStatus?.[RequestStatus.OPEN] ?? 0,
        tone: 'open',
      },
      {
        label: this.statusLabel(RequestStatus.IN_PROGRESS),
        shortLabel: 'Andamento',
        value: this.dashboard?.byStatus?.[RequestStatus.IN_PROGRESS] ?? 0,
        tone: 'progress',
      },
      {
        label: this.statusLabel(RequestStatus.WAITING_INFO),
        shortLabel: 'Aguardando',
        value: this.dashboard?.byStatus?.[RequestStatus.WAITING_INFO] ?? 0,
        tone: 'waiting',
      },
      {
        label: this.statusLabel(RequestStatus.RESOLVED),
        shortLabel: 'Concluidas',
        value: this.dashboard?.byStatus?.[RequestStatus.RESOLVED] ?? 0,
        tone: 'resolved',
      },
    ];
  }

  protected get maxStatusVolume(): number {
    return Math.max(...this.statusVolume.map((item) => item.value), 1);
  }

  protected get categoryVolume(): CategoryItem[] {
    return Object.values(RequestCategory).map((category) => ({
      label: categoryLabel(category),
      value: this.dashboard?.byCategory?.[category] ?? 0,
    }));
  }

  protected get slaAlerts(): RequestFlowRequest[] {
    return this.dashboard?.slaAlerts.slice(0, 4) ?? [];
  }

  protected barHeight(item: DistributionItem): number {
    return Math.max((item.value / this.maxStatusVolume) * 240, 28);
  }

  private statusLabel(status: RequestStatus): string {
    return statusLabel(status);
  }
}
