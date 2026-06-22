import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { CurrentUserService, TokenService } from '../services';
import { Role } from '../models';

describe('authGuard', () => {
  const routerMock = {
    createUrlTree: vi.fn((commands: unknown[], extras?: unknown) => ({ commands, extras })),
  };

  beforeEach(() => {
    localStorage.clear();
    routerMock.createUrlTree.mockClear();
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: routerMock }],
    });
  });

  it('should allow access with token and current user', () => {
    TestBed.inject(TokenService).saveToken('fake-token');
    TestBed.inject(CurrentUserService).setCurrentUser({
      id: 'usr-001',
      name: 'User',
      email: 'user@requestflow.com',
      role: Role.USER,
      department: 'Financeiro',
      birthDate: '1992-04-18',
      active: true,
      createdAt: '2026-01-10T09:00:00-03:00',
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/dashboard' } as never),
    );

    expect(result).toBe(true);
  });

  it('should block access without token', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/dashboard' } as never),
    );

    expect(result).toEqual({
      commands: ['/auth/login'],
      extras: { queryParams: { returnUrl: '/dashboard' } },
    });
  });
});
