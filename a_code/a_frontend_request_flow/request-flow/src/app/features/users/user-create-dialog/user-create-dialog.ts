import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Role, User } from '../../../core/models';

export interface UserCreateDialogResult {
  name: string;
  email: string;
  role: Role;
  birthDate: Date;
  active: boolean;
}

@Component({
  selector: 'app-user-create-dialog',
  imports: [
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './user-create-dialog.html',
  styleUrl: './user-create-dialog.scss',
})
export class UserCreateDialogComponent {
  private readonly dialogRef = inject<MatDialogRef<UserCreateDialogComponent, UserCreateDialogResult>>(MatDialogRef);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly roleOptions = Object.values(Role);
  protected readonly userForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    role: [Role.USER, [Validators.required]],
    birthDate: [new Date(1990, 0, 1), [Validators.required]],
    active: true,
  });

  protected save(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.userForm.getRawValue());
  }

  protected roleLabel(role: Role): string {
    const labels: Record<Role, string> = {
      [Role.USER]: 'Usuario',
      [Role.ANALYST]: 'Analista',
      [Role.MANAGER]: 'Gestor',
    };

    return labels[role];
  }
}
