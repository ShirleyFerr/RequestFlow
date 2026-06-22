import { Component, input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';

export type SlaBadgeState = 'overdue' | 'near' | 'ok' | 'closed';

@Component({
  selector: 'app-sla-badge',
  imports: [MatChipsModule],
  template: '<mat-chip class="request-badge request-badge--sla-{{ state() }}">{{ label }}</mat-chip>',
  styleUrl: './request-badges.scss',
})
export class SlaBadgeComponent {
  readonly state = input.required<SlaBadgeState>();

  get label(): string {
    const labels: Record<SlaBadgeState, string> = {
      overdue: 'Vencido',
      near: 'Próximo do vencimento',
      ok: 'Dentro do prazo',
      closed: 'Encerrado',
    };

    return labels[this.state()];
  }
}
