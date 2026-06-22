import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  Request as RequestFlowRequest,
  RequestCategory,
  RequestPriority,
  RequestStatus,
} from '../../../core/models';
import { CurrentUserService, PermissionService, RequestService } from '../../../core/services';
import {
  EmptyStateComponent,
  PriorityBadgeComponent,
  SlaBadgeComponent,
  StatusBadgeComponent,
} from '../../../shared/components';
import { categoryLabel, priorityLabel, statusLabel } from '../../../shared/pipes';
import {
  ReassignRequestDialogComponent,
  ReassignRequestDialogResult,
} from './reassign-request-dialog';
import {
  StatusUpdateDialogComponent,
  StatusUpdateDialogResult,
} from './status-update-dialog';

type SlaState = 'overdue' | 'near' | 'ok' | 'closed';

@Component({
  selector: 'app-request-detail',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    EmptyStateComponent,
    PriorityBadgeComponent,
    ReactiveFormsModule,
    RouterLink,
    SlaBadgeComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './request-detail.html',
  styleUrl: './request-detail.scss',
})
export class RequestDetailComponent {
  private readonly currentUserService = inject(CurrentUserService);
  private readonly dialog = inject(MatDialog);
  private readonly formBuilder = inject(FormBuilder);
  private readonly permissionService = inject(PermissionService);
  private readonly requestService = inject(RequestService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly today = this.toDateOnly(new Date());

  protected readonly status = RequestStatus;
  protected readonly currentUser = this.currentUserService.currentUser;
  protected readonly request = signal<RequestFlowRequest | null>(null);
  protected readonly commentForm = this.formBuilder.nonNullable.group({
    message: ['', [Validators.required]],
  });

  protected readonly orderedHistory = computed(() =>
    [...(this.request()?.statusHistory ?? [])].sort(
      (a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime(),
    ),
  );

  constructor() {
    this.loadRequest();
  }

  protected addComment(): void {
    const request = this.request();
    const currentUser = this.currentUser();

    if (!request || !currentUser || !this.permissionService.canComment(currentUser.role, request)) {
      return;
    }

    if (this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
      return;
    }

    this.requestService.addComment(request.id, this.commentForm.getRawValue().message.trim()).subscribe({
      next: (updatedRequest) => {
        this.request.set(updatedRequest);
        this.commentForm.reset({ message: '' });
        this.snackBar.open('Comentario adicionado.', 'Fechar', { duration: 3000 });
      },
    });
  }

  protected openStatusDialog(): void {
    const request = this.request();

    if (!request || !this.canUpdateStatus(request)) {
      return;
    }

    this.dialog
      .open<StatusUpdateDialogComponent, { currentStatus: RequestStatus }, StatusUpdateDialogResult>(
        StatusUpdateDialogComponent,
        {
          data: { currentStatus: request.status },
        },
      )
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.updateStatus(request, result);
        }
      });
  }

  protected assumeRequest(): void {
    const request = this.request();
    const currentUser = this.currentUser();

    if (!request || !currentUser || !this.permissionService.canAssumeRequest(currentUser.role, request)) {
      return;
    }

    this.requestService.assumeRequest(request.id).subscribe({
      next: (updatedRequest) => {
        this.request.set(updatedRequest);
        this.snackBar.open('Solicitacao assumida com sucesso.', 'Fechar', {
          duration: 4000,
        });
      },
      error: () => {
        this.snackBar.open('Nao foi possivel assumir esta solicitacao.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected openReassignDialog(): void {
    const request = this.request();
    const currentUser = this.currentUser();

    if (!request || !currentUser || !this.permissionService.canReassignRequest(currentUser.role, request)) {
      return;
    }

    this.dialog
      .open<ReassignRequestDialogComponent, undefined, ReassignRequestDialogResult>(
        ReassignRequestDialogComponent,
      )
      .afterClosed()
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.requestService.reassignRequest(request.id, result.analystId).subscribe({
          next: (updatedRequest) => {
            this.request.set(updatedRequest);
            this.snackBar.open('Responsavel reatribuido com sucesso.', 'Fechar', {
              duration: 4000,
            });
          },
          error: () => {
            this.snackBar.open('Nao foi possivel reatribuir esta solicitacao.', 'Fechar', {
              duration: 4000,
            });
          },
        });
      });
  }

  protected canUpdateStatus(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canChangeStatus(user.role, request);
  }

  protected canAssume(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canAssumeRequest(user.role, request);
  }

  protected canReassign(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canReassignRequest(user.role, request);
  }

  protected canAddComment(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canComment(user.role, request);
  }

  protected slaState(request: RequestFlowRequest): SlaState {
    if ([RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)) {
      return 'closed';
    }

    const dueDate = this.toDateOnly(new Date(request.dueDate));
    const daysToDue = Math.ceil((dueDate - this.today) / 86_400_000);

    if (daysToDue < 0) {
      return 'overdue';
    }

    if (daysToDue <= 3) {
      return 'near';
    }

    return 'ok';
  }

  protected slaLabel(request: RequestFlowRequest): string {
    const labels: Record<SlaState, string> = {
      overdue: 'Vencido',
      near: 'Proximo do vencimento',
      ok: 'Dentro do prazo',
      closed: 'Encerrado',
    };

    return labels[this.slaState(request)];
  }

  protected statusLabel(status: RequestStatus): string {
    return statusLabel(status);
  }

  protected priorityLabel(priority: RequestPriority): string {
    return priorityLabel(priority);
  }

  protected categoryLabel(category: RequestCategory): string {
    return categoryLabel(category);
  }

  private updateStatus(request: RequestFlowRequest, result: StatusUpdateDialogResult): void {
    this.requestService.updateStatus(request.id, result.status, result.note.trim() || undefined).subscribe({
      next: (updatedRequest) => {
        this.request.set(updatedRequest);
        this.snackBar.open('Status atualizado.', 'Fechar', { duration: 3000 });
      },
    });
  }

  private canView(request: RequestFlowRequest): boolean {
    const user = this.currentUser();

    if (!user) {
      return false;
    }

    return this.permissionService.canViewRequest(user.role, request, user);
  }

  private loadRequest(): void {
    const requestId = this.route.snapshot.paramMap.get('id');

    if (!requestId) {
      void this.router.navigate(['/requests']);
      return;
    }

    this.requestService.getRequestById(requestId).subscribe({
      next: (foundRequest) => {
        if (!this.canView(foundRequest)) {
          this.handleUnavailableRequest();
          return;
        }

        this.request.set(foundRequest);
      },
      error: () => this.handleUnavailableRequest(),
    });
  }

  private handleUnavailableRequest(): void {
    this.snackBar.open('Solicitacao nao encontrada ou sem permissao de acesso.', 'Fechar', {
      duration: 4000,
    });
    void this.router.navigate(['/requests']);
  }

  private toDateOnly(date: Date): number {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  }
}
