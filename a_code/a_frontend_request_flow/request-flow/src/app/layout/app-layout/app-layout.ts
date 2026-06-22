import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { SidebarComponent } from '../sidebar/sidebar';
import { TopbarComponent } from '../topbar/topbar';

@Component({
  selector: 'app-layout',
  imports: [MatSidenavModule, RouterOutlet, SidebarComponent, TopbarComponent],
  templateUrl: './app-layout.html',
  styleUrl: './app-layout.scss',
})
export class AppLayoutComponent {
  private readonly breakpointObserver = inject(BreakpointObserver);

  protected readonly isSidebarCollapsed = signal(false);

  protected readonly isHandset = toSignal(
    this.breakpointObserver.observe('(max-width: 900px)').pipe(map((state) => state.matches)),
    { initialValue: false },
  );

  protected toggleSidebarCollapsed(): void {
    this.isSidebarCollapsed.update((collapsed) => !collapsed);
  }
}
