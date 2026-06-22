import { Pipe, PipeTransform } from '@angular/core';
import { RequestCategory, RequestPriority, RequestStatus } from '../../core/models';

export function statusLabel(status: RequestStatus): string {
  const labels: Record<RequestStatus, string> = {
    [RequestStatus.OPEN]: 'Aberta',
    [RequestStatus.IN_PROGRESS]: 'Em andamento',
    [RequestStatus.WAITING_INFO]: 'Aguardando informações',
    [RequestStatus.RESOLVED]: 'Resolvida',
    [RequestStatus.CANCELLED]: 'Cancelada',
  };

  return labels[status];
}

export function priorityLabel(priority: RequestPriority): string {
  const labels: Record<RequestPriority, string> = {
    [RequestPriority.LOW]: 'Baixa',
    [RequestPriority.MEDIUM]: 'Média',
    [RequestPriority.HIGH]: 'Alta',
    [RequestPriority.CRITICAL]: 'Crítica',
  };

  return labels[priority];
}

export function categoryLabel(category: RequestCategory): string {
  const labels: Record<RequestCategory, string> = {
    [RequestCategory.ACCESS]: 'Acesso',
    [RequestCategory.BUG]: 'Erro',
    [RequestCategory.REQUEST]: 'Solicitação',
    [RequestCategory.INCIDENT]: 'Incidente',
    [RequestCategory.SUPPORT]: 'Suporte',
    [RequestCategory.OTHER]: 'Outros',
  };

  return labels[category];
}

@Pipe({
  name: 'statusLabel',
})
export class StatusLabelPipe implements PipeTransform {
  transform(status: RequestStatus): string {
    return statusLabel(status);
  }
}

@Pipe({
  name: 'priorityLabel',
})
export class PriorityLabelPipe implements PipeTransform {
  transform(priority: RequestPriority): string {
    return priorityLabel(priority);
  }
}

@Pipe({
  name: 'categoryLabel',
})
export class CategoryLabelPipe implements PipeTransform {
  transform(category: RequestCategory): string {
    return categoryLabel(category);
  }
}
