import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CurrentUserService } from '../services';
import { TokenService } from '../services/token.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);
  const tokenService = inject(TokenService);

  if (tokenService.getToken() && currentUserService.getCurrentUser()) {
    return true;
  }

  return router.createUrlTree(['/auth/login'], {
    queryParams: { returnUrl: state.url },
  });
};
