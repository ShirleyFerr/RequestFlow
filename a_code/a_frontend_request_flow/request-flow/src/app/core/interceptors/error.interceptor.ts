import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { CurrentUserService, TokenService } from '../services';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);
  const tokenService = inject(TokenService);

  return next(request).pipe(
    catchError((error: unknown) => {
      const message =
        error instanceof HttpErrorResponse
          ? buildHttpErrorMessage(error)
          : 'Ocorreu um erro inesperado.';

      snackBar.open(message, 'Fechar', {
        duration: 4000,
      });

      if (error instanceof HttpErrorResponse && error.status === 401 && !request.url.includes('/auth/login')) {
        tokenService.removeToken();
        currentUserService.clearCurrentUser();
        void router.navigate(['/auth/login']);
      }

      return throwError(() => error);
    }),
  );
};

function buildHttpErrorMessage(error: HttpErrorResponse): string {
  if (error.status === 401) {
    return 'Sessao expirada ou credenciais invalidas.';
  }

  if (error.status === 403) {
    return 'Voce nao tem permissao para executar esta acao.';
  }

  const backendMessage = error.error?.message;

  if (backendMessage) {
    return backendMessage;
  }

  const fieldError = error.error?.fieldErrors?.[0]?.message;

  if (fieldError) {
    return fieldError;
  }

  return 'Nao foi possivel concluir a operacao.';
}
