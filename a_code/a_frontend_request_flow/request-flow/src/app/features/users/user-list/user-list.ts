import { DatePipe } from '@angular/common';
import { Component, ViewChild, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTable, MatTableModule } from '@angular/material/table';
import { Role, User } from '../../../core/models';
import { UserService } from '../../../core/services';
import { EmptyStateComponent, PageHeaderComponent } from '../../../shared/components';
import {
  UserCreateDialogComponent,
  UserCreateDialogResult,
} from '../user-create-dialog/user-create-dialog';

@Component({
  selector: 'app-user-list',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    EmptyStateComponent,
    MatIconModule,
    MatSnackBarModule,
    MatTableModule,
    PageHeaderComponent,
  ],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss',
})
export class UserListComponent {
  @ViewChild(MatTable) private table?: MatTable<User>;

  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly userService = inject(UserService);

  protected readonly displayedColumns = ['id', 'name', 'email', 'role', 'active', 'createdAt', 'actions'];
  protected users: User[] = [];

  constructor() {
    this.loadUsers();
  }

  protected openCreateDialog(): void {
    this.dialog
      .open<UserCreateDialogComponent, undefined, UserCreateDialogResult>(UserCreateDialogComponent)
      .afterClosed()
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.userService
          .createUser({
            name: result.name.trim(),
            email: result.email.trim().toLowerCase(),
            role: result.role,
            birthDate: this.formatDate(result.birthDate),
            active: result.active,
          })
          .subscribe({
            next: () => {
              this.loadUsers();
              this.snackBar.open('Usuario cadastrado com sucesso.', 'Fechar', {
                duration: 4000,
              });
            },
            error: () => {
              this.snackBar.open('Nao foi possivel cadastrar o usuario.', 'Fechar', {
                duration: 4000,
              });
            },
          });
      });
  }

  protected roleLabel(role: Role): string {
    const labels: Record<Role, string> = {
      [Role.USER]: 'Usuario',
      [Role.ANALYST]: 'Analista',
      [Role.MANAGER]: 'Gestor',
    };

    return labels[role];
  }

  private loadUsers(): void {
    this.userService.listUsers().subscribe((users) => {
      this.users = users;
      this.table?.renderRows();
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
