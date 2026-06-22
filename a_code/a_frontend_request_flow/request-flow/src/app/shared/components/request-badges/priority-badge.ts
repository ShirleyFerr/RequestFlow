import { Component, input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { RequestPriority } from '../../../core/models';
import { priorityLabel } from '../../pipes/request-labels';

@Component({
  selector: 'app-priority-badge',
  imports: [MatChipsModule],
  template: '<mat-chip class="request-badge request-badge--priority-{{ priority().toLowerCase() }}">{{ label }}</mat-chip>',
  styleUrl: './request-badges.scss',
})
export class PriorityBadgeComponent {
  readonly priority = input.required<RequestPriority>();

  get label(): string {
    return priorityLabel(this.priority());
  }
}
