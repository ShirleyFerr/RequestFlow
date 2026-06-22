import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, throwError, timeout } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AISuggestion } from '../models';

export interface AISuggestionInput {
  title?: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class AIService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  suggestRequest(input: AISuggestionInput): Observable<AISuggestion> {
    return this.http.post<AISuggestion>(`${this.apiUrl}/requests/ai-suggestion`, {
      description: input.description.trim(),
    }).pipe(
      timeout(5000),
      catchError(() =>
        throwError(() => new Error('Nao foi possivel gerar a sugestao agora.')),
      ),
    );
  }
}
