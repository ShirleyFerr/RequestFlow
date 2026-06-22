import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService, CurrentUserService } from '../../core/services';

@Component({
  selector: 'app-topbar',
  imports: [MatButtonModule, MatIconModule, MatMenuModule, MatToolbarModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class TopbarComponent {
  @Input() showMenuButton = false;
  @Output() readonly toggleSidebar = new EventEmitter<void>();

  private readonly authService = inject(AuthService);
  private readonly currentUserService = inject(CurrentUserService);

  protected readonly currentUser = this.currentUserService.currentUser;

  protected logout(): void {
    this.authService.logout();
  }
}
