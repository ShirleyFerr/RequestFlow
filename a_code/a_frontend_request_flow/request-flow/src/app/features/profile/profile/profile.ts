import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Role } from '../../../core/models';
import { CurrentUserService, Profile, ProfileService } from '../../../core/services';
import { LoadingComponent, PageHeaderComponent } from '../../../shared/components';

@Component({
  selector: 'app-profile',
  imports: [
    DatePipe,
    LoadingComponent,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    PageHeaderComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class ProfileComponent implements OnInit {
  private readonly currentUserService = inject(CurrentUserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly snackBar = inject(MatSnackBar);

  protected profile?: Profile;
  protected loading = true;
  protected refreshing = false;
  protected savingPassword = false;
  protected showPasswordForm = false;

  protected readonly passwordForm = this.formBuilder.nonNullable.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    {
      validators: this.samePasswordValidator,
    },
  );

  ngOnInit(): void {
    this.useCachedUser();
    this.loadProfile();
  }

  protected roleLabel(role: Role): string {
    const labels: Record<Role, string> = {
      [Role.USER]: 'Usuario',
      [Role.ANALYST]: 'Analista',
      [Role.MANAGER]: 'Gestor',
    };

    return labels[role];
  }

  protected togglePasswordForm(): void {
    this.showPasswordForm = !this.showPasswordForm;
    this.passwordForm.reset();
  }

  protected changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.savingPassword = true;
    this.profileService.changePassword(this.passwordForm.getRawValue()).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.savingPassword = false;
        this.showPasswordForm = false;
        this.passwordForm.reset();
        this.snackBar.open('Senha alterada com sucesso.', 'Fechar', {
          duration: 4000,
        });
      },
      error: () => {
        this.savingPassword = false;
        this.snackBar.open('Nao foi possivel alterar a senha.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  private loadProfile(): void {
    this.refreshing = Boolean(this.profile);

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
        this.refreshing = false;
      },
      error: () => {
        this.loading = false;
        this.refreshing = false;
        this.snackBar.open('Nao foi possivel carregar o perfil.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  private samePasswordValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (!newPassword || !confirmPassword || newPassword === confirmPassword) {
      return null;
    }

      return { passwordsMismatch: true };
  }

  private useCachedUser(): void {
    const cachedUser = this.currentUserService.getCurrentUser();

    if (!cachedUser) {
      return;
    }

    this.profile = {
      id: String(cachedUser.id),
      name: cachedUser.name,
      email: cachedUser.email,
      role: cachedUser.role,
      active: cachedUser.active,
      createdAt: cachedUser.createdAt,
      birthDate: cachedUser.birthDate,
    };
    this.loading = false;
  }
}
