import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RequestStatus } from '../../../core/models';
import { statusLabel } from '../../../shared/pipes';

export interface StatusUpdateDialogData {
  currentStatus: RequestStatus;
}

export interface StatusUpdateDialogResult {
  status: RequestStatus;
  note: string;
}

@Component({
  selector: 'app-status-update-dialog',
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>Atualizar status</h2>
    <mat-dialog-content>
      <form class="dialog-form" [formGroup]="statusForm">
        <mat-form-field appearance="outline">
          <mat-label>Novo status</mat-label>
          <mat-select formControlName="status">
            @for (status of statusOptions; track status) {
              <mat-option [value]="status">{{ statusLabel(status) }}</mat-option>
            }
          </mat-select>
          @if (statusForm.controls.status.hasError('required')) {
            <mat-error>Selecione o novo status.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Observação</mat-label>
          <textarea matInput rows="4" formControlName="note"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton="outlined" type="button" mat-dialog-close>Cancelar</button>
      <button matButton="filled" type="button" (click)="confirm()">Salvar</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-form {
        display: grid;
        min-width: min(520px, 80vw);
        gap: 16px;
        padding-top: 8px;
      }
    `,
  ],
})
export class StatusUpdateDialogComponent {
  private readonly data = inject<StatusUpdateDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef =
    inject<MatDialogRef<StatusUpdateDialogComponent, StatusUpdateDialogResult>>(MatDialogRef);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly statusOptions = Object.values(RequestStatus);

  protected readonly statusForm = this.formBuilder.nonNullable.group({
    status: [this.data.currentStatus, [Validators.required]],
    note: '',
  });

  protected confirm(): void {
    if (this.statusForm.invalid) {
      this.statusForm.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.statusForm.getRawValue());
  }

  protected statusLabel(status: RequestStatus): string {
    return statusLabel(status);
  }
}
