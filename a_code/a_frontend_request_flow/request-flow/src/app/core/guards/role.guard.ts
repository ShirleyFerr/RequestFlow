import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models';
import { CurrentUserService, PermissionService } from '../services';

export const roleGuard: CanActivateFn = (route) => {
  const currentUserService = inject(CurrentUserService);
  const permissionService = inject(PermissionService);
  const router = inject(Router);
  const allowedRoles = route.data['roles'] as Role[] | undefined;
  const currentUser = currentUserService.getCurrentUser();

  if (!currentUser) {
    return router.createUrlTree(['/auth/login']);
  }

  if (!allowedRoles?.length) {
    return true;
  }

  if (allowedRoles.includes(currentUser.role)) {
    return true;
  }

  return router.createUrlTree([permissionService.getDefaultRouteByRole(currentUser.role)]);
};
