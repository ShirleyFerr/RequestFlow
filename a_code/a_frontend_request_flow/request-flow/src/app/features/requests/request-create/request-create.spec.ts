import { provideNativeDateAdapter } from '@angular/material/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { MOCK_USERS } from '../../../core/mocks';
import { RequestCategory, RequestPriority, RequestStatus } from '../../../core/models';
import { AIService, CurrentUserService, RequestService } from '../../../core/services';
import { RequestCreateComponent } from './request-create';

describe('RequestCreateComponent', () => {
  let fixture: ComponentFixture<RequestCreateComponent>;
  let component: RequestCreateComponent;
  let requestService: { createRequest: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    localStorage.clear();
    requestService = {
      createRequest: vi.fn().mockReturnValue(
        of({
          id: 'req-new',
          title: 'Nova solicitacao de acesso',
          description: 'Preciso de acesso ao sistema interno.',
          summary: 'Preciso de acesso ao sistema interno.',
          category: RequestCategory.ACCESS,
          priority: RequestPriority.HIGH,
          status: RequestStatus.OPEN,
          requester: MOCK_USERS[0],
          dueDate: '2026-07-10T23:59:00',
          createdAt: '2026-06-17T10:00:00',
          updatedAt: '2026-06-17T10:00:00',
          comments: [],
          statusHistory: [],
        }),
      ),
    };
    router = {
      navigate: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RequestCreateComponent],
      providers: [
        provideAnimationsAsync(),
        provideNativeDateAdapter(),
        {
          provide: Router,
          useValue: router,
        },
        {
          provide: RequestService,
          useValue: requestService,
        },
        {
          provide: AIService,
          useValue: {
            suggestRequest: vi.fn(),
          },
        },
        {
          provide: MatSnackBar,
          useValue: {
            open: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    TestBed.inject(CurrentUserService).setCurrentUser(MOCK_USERS[0]);
    fixture = TestBed.createComponent(RequestCreateComponent);
    component = fixture.componentInstance;
  });

  it('should create a request with required fields and dueDate', () => {
    component['requestForm'].setValue({
      title: 'Nova solicitacao de acesso',
      description: 'Preciso de acesso ao sistema interno.',
      category: RequestCategory.ACCESS,
      priority: RequestPriority.HIGH,
      dueDate: new Date(2026, 6, 10),
      aiSummary: '',
    });

    component['save']();

    expect(requestService.createRequest).toHaveBeenCalledWith({
      title: 'Nova solicitacao de acesso',
      description: 'Preciso de acesso ao sistema interno.',
      category: RequestCategory.ACCESS,
      priority: RequestPriority.HIGH,
      dueDate: '2026-07-10T23:59:00',
      aiSummary: 'Preciso de acesso ao sistema interno.',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should apply AI suggestion while keeping fields editable', () => {
    component['suggestion'] = {
      category: RequestCategory.BUG,
      priority: RequestPriority.CRITICAL,
      summary: 'Erro critico em sistema',
      confidence: 0.9,
      generatedAt: new Date().toISOString(),
    };

    component['applySuggestion']();
    component['requestForm'].patchValue({ priority: RequestPriority.MEDIUM });

    expect(component['requestForm'].controls.category.value).toBe(RequestCategory.BUG);
    expect(component['requestForm'].controls.priority.value).toBe(RequestPriority.MEDIUM);
    expect(component['requestForm'].controls.aiSummary.value).toBe('Erro critico em sistema');
  });
});
