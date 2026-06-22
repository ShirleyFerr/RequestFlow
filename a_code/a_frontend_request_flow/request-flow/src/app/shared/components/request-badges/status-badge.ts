import { Component, input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { RequestStatus } from '../../../core/models';
import { statusLabel } from '../../pipes/request-labels';

@Component({
  selector: 'app-status-badge',
  imports: [MatChipsModule],
  template: '<mat-chip class="request-badge request-badge--status-{{ status().toLowerCase() }}">{{ label }}</mat-chip>',
  styleUrl: './request-badges.scss',
})
export class StatusBadgeComponent {
  readonly status = input.required<RequestStatus>();

  get label(): string {
    return statusLabel(this.status());
  }
}
