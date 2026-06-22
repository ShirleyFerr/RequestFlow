import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, CurrentUserService, PermissionService, TokenService } from '../../../core/services';

@Component({
  selector: 'app-login',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    ReactiveFormsModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly currentUserService = inject(CurrentUserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly permissionService = inject(PermissionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly tokenService = inject(TokenService);

  protected readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected hidePassword = true;
  protected readonly isSubmitting = signal(false);

  constructor() {
    const currentUser = this.currentUserService.getCurrentUser();

    if (currentUser && this.tokenService.getToken()) {
      void this.router.navigate([this.permissionService.getDefaultRouteByRole(currentUser.role)]);
    }
  }

  protected get emailErrorMessage(): string {
    const email = this.loginForm.controls.email;

    if (email.hasError('required')) {
      return 'Informe o e-mail.';
    }

    if (email.hasError('email')) {
      return 'Informe um e-mail valido.';
    }

    return '';
  }

  protected submit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.authService
      .login(this.loginForm.getRawValue())
      .subscribe({
        next: (result) => {
          const returnUrl =
            this.route.snapshot.queryParamMap.get('returnUrl') ??
            this.permissionService.getDefaultRouteByRole(result.user.role);
          void this.router.navigateByUrl(returnUrl);
        },
        error: (error: Error) => {
          queueMicrotask(() => this.isSubmitting.set(false));
          this.snackBar.open(error.message, 'Fechar', {
            duration: 4000,
          });
        },
      });
  }
}
