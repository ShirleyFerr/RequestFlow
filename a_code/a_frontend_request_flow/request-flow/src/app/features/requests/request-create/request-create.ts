import { PercentPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  AISuggestion,
  RequestCategory,
  RequestPriority,
} from '../../../core/models';
import { AIService, CurrentUserService, RequestService } from '../../../core/services';
import { PageHeaderComponent } from '../../../shared/components';
import { categoryLabel, priorityLabel } from '../../../shared/pipes';

@Component({
  selector: 'app-request-create',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    PageHeaderComponent,
    PercentPipe,
    ReactiveFormsModule,
  ],
  templateUrl: './request-create.html',
  styleUrl: './request-create.scss',
})
export class RequestCreateComponent {
  private readonly aiService = inject(AIService);
  private readonly currentUserService = inject(CurrentUserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly requestService = inject(RequestService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly categoryOptions = Object.values(RequestCategory);
  protected readonly priorityOptions = Object.values(RequestPriority);
  protected readonly minDueDate = this.tomorrow();
  protected suggestion: AISuggestion | null = null;
  protected isSuggesting = false;

  protected readonly requestForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required]],
    description: ['', [Validators.required]],
    category: [RequestCategory.OTHER, [Validators.required]],
    priority: [RequestPriority.MEDIUM, [Validators.required]],
    dueDate: [this.tomorrow(), [Validators.required]],
    aiSummary: [''],
  });

  protected suggestWithAI(): void {
    if (this.isSuggesting) {
      return;
    }

    const { title, description } = this.requestForm.getRawValue();

    if (!title.trim() || !description.trim()) {
      this.requestForm.controls.title.markAsTouched();
      this.requestForm.controls.description.markAsTouched();
      this.snackBar.open('Informe titulo e descricao para gerar a sugestao.', 'Fechar', {
        duration: 4000,
      });
      return;
    }

    this.isSuggesting = true;
    this.suggestion = null;

    this.aiService
      .suggestRequest({ title, description })
      .pipe(finalize(() => (this.isSuggesting = false)))
      .subscribe({
        next: (suggestion) => {
          this.suggestion = suggestion;
          this.snackBar.open('Sugestao de IA gerada.', 'Fechar', {
            duration: 3000,
          });
        },
        error: (error: Error) => {
          this.suggestion = null;
          this.snackBar.open(`${error.message} Continue preenchendo manualmente.`, 'Fechar', {
            duration: 5000,
          });
        },
      });
  }

  protected applySuggestion(): void {
    if (!this.suggestion) {
      return;
    }

    this.requestForm.patchValue({
      category: this.suggestion.category,
      priority: this.suggestion.priority,
      aiSummary: this.suggestion.summary,
    });

    this.snackBar.open('Sugestao aplicada. Categoria, prioridade e resumo continuam editaveis.', 'Fechar', {
      duration: 4000,
    });
  }

  protected save(): void {
    if (this.requestForm.invalid) {
      this.requestForm.markAllAsTouched();
      return;
    }

    const currentUser = this.currentUserService.getCurrentUser();

    if (!currentUser) {
      this.snackBar.open('Sessao expirada. Faca login novamente.', 'Fechar', {
        duration: 4000,
      });
      void this.router.navigate(['/auth/login']);
      return;
    }

    const formValue = this.requestForm.getRawValue();

    this.requestService
      .createRequest({
        title: formValue.title.trim(),
        description: formValue.description.trim(),
        category: formValue.category,
        priority: formValue.priority,
        dueDate: this.toLocalDateTime(formValue.dueDate),
        aiSummary: formValue.aiSummary.trim() || this.createManualSummary(formValue.description),
      })
      .subscribe({
        next: () => {
          this.snackBar.open('Solicitacao criada com sucesso.', 'Fechar', {
            duration: 4000,
          });
          void this.router.navigate(['/requests']);
        },
        error: (error: unknown) => {
          this.snackBar.open(this.buildCreateErrorMessage(error), 'Fechar', {
            duration: 4000,
          });
        },
      });
  }

  protected categoryLabel(category: RequestCategory): string {
    return categoryLabel(category);
  }

  protected priorityLabel(priority: RequestPriority): string {
    return priorityLabel(priority);
  }

  private createManualSummary(description: string): string {
    const trimmedDescription = description.trim();
    return trimmedDescription.length <= 120
      ? trimmedDescription
      : `${trimmedDescription.slice(0, 117)}...`;
  }

  private toLocalDateTime(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}T23:59:00`;
  }

  private tomorrow(): Date {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    date.setHours(0, 0, 0, 0);
    return date;
  }

  private buildCreateErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const fieldMessage = error.error?.fieldErrors?.[0]?.message;
      const backendMessage = error.error?.message;

      if (fieldMessage) {
        return fieldMessage;
      }

      if (backendMessage) {
        return backendMessage;
      }
    }

    return 'Nao foi possivel criar a solicitacao.';
  }
}
