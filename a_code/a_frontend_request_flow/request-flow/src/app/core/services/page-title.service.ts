import { Injectable, inject } from '@angular/core';
import { Role } from '../models';
import { PermissionService } from './permission.service';

@Injectable({
  providedIn: 'root',
})
export class PageTitleService {
  private readonly permissionService = inject(PermissionService);

  requestsTitle(role: Role): string {
    return this.permissionService.getRequestsTitleByRole(role);
  }
}
