import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService, CurrentUserService, PermissionService } from '../../core/services';

@Component({
  selector: 'app-sidebar',
  imports: [MatButtonModule, MatIconModule, MatListModule, MatTooltipModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() showCollapseButton = false;
  @Output() readonly navigate = new EventEmitter<void>();
  @Output() readonly toggleCollapsed = new EventEmitter<void>();

  private readonly authService = inject(AuthService);
  private readonly currentUserService = inject(CurrentUserService);
  protected readonly permissionService = inject(PermissionService);

  protected readonly currentUser = this.currentUserService.currentUser;

  protected logout(): void {
    this.authService.logout();
    this.navigate.emit();
  }
}
