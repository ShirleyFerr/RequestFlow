import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { RequestService } from '../../../core/services';
import { User } from '../../../core/models';

export interface ReassignRequestDialogResult {
  analystId: string;
}

@Component({
  selector: 'app-reassign-request-dialog',
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatSelectModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>Reatribuir responsável</h2>
    <mat-dialog-content>
      <form class="dialog-form" [formGroup]="reassignForm">
        <mat-form-field appearance="outline">
          <mat-label>Novo responsável</mat-label>
          <mat-select formControlName="analystId">
            @for (analyst of analysts(); track analyst.id) {
              <mat-option [value]="analyst.id">{{ analyst.name }}</mat-option>
            }
          </mat-select>
          @if (reassignForm.controls.analystId.hasError('required')) {
            <mat-error>Selecione um analista.</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton="outlined" type="button" mat-dialog-close>Cancelar</button>
      <button matButton="filled" type="button" (click)="confirm()">Reatribuir</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-form {
        display: grid;
        min-width: min(480px, 80vw);
        padding-top: 8px;
      }
    `,
  ],
})
export class ReassignRequestDialogComponent {
  private readonly dialogRef =
    inject<MatDialogRef<ReassignRequestDialogComponent, ReassignRequestDialogResult>>(MatDialogRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly requestService = inject(RequestService);

  protected readonly analysts = signal<User[]>([]);
  protected readonly reassignForm = this.formBuilder.nonNullable.group({
    analystId: ['', [Validators.required]],
  });

  constructor() {
    this.requestService.getActiveAnalysts().subscribe((analysts) => {
      this.analysts.set(analysts);
    });
  }

  protected confirm(): void {
    if (this.reassignForm.invalid) {
      this.reassignForm.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.reassignForm.getRawValue());
  }
}
