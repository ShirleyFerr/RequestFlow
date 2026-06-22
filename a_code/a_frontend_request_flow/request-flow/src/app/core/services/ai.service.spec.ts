import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { AIService } from './ai.service';
import { RequestCategory, RequestPriority } from '../models';
import { environment } from '../../../environments/environment';

describe('AIService', () => {
  let service: AIService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AIService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should suggest category, priority and summary', async () => {
    const suggestionPromise = firstValueFrom(
      service.suggestRequest({
        title: 'Erro critico no relatorio',
        description: 'Sistema parado ao emitir relatorio financeiro urgente.',
      }),
    );
    const request = httpMock.expectOne(`${environment.apiUrl}/requests/ai-suggestion`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body.description).toContain('Erro critico no relatorio');
    request.flush({
      category: RequestCategory.BUG,
      priority: RequestPriority.CRITICAL,
      summary: 'Sistema parado ao emitir relatorio financeiro urgente.',
    });

    const suggestion = await suggestionPromise;

    expect(suggestion.category).toBe(RequestCategory.BUG);
    expect(suggestion.priority).toBe(RequestPriority.CRITICAL);
    expect(suggestion.summary).toContain('Sistema parado');
  });

  it('should fail predictably for fallback scenario', async () => {
    const suggestionPromise = firstValueFrom(
      service.suggestRequest({
        title: 'falha ia',
        description: 'Forcar fallback manual',
      }),
    );
    const request = httpMock.expectOne(`${environment.apiUrl}/requests/ai-suggestion`);
    request.flush({ message: 'IA indisponivel' }, { status: 500, statusText: 'Server Error' });

    await expect(suggestionPromise).rejects.toThrow('Nao foi possivel gerar a sugestao agora.');
  });
});
