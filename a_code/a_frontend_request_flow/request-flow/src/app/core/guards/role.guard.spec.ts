import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { roleGuard } from './role.guard';
import { CurrentUserService } from '../services';
import { Role, User } from '../models';

describe('roleGuard', () => {
  const routerMock = {
    createUrlTree: vi.fn((commands: unknown[]) => ({ commands })),
  };

  const route = { data: { roles: [Role.MANAGER] } };

  beforeEach(() => {
    localStorage.clear();
    routerMock.createUrlTree.mockClear();
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: routerMock }],
    });
  });

  it('should allow MANAGER to access users route', () => {
    setCurrentUser(Role.MANAGER);

    const result = TestBed.runInInjectionContext(() => roleGuard(route as never, {} as never));

    expect(result).toBe(true);
  });

  it('should block USER from users route and redirect to requests', () => {
    setCurrentUser(Role.USER);

    const result = TestBed.runInInjectionContext(() => roleGuard(route as never, {} as never));

    expect(result).toEqual({ commands: ['/requests'] });
  });

  it('should block ANALYST from users route and redirect to dashboard', () => {
    setCurrentUser(Role.ANALYST);

    const result = TestBed.runInInjectionContext(() => roleGuard(route as never, {} as never));

    expect(result).toEqual({ commands: ['/dashboard'] });
  });

  it.each([Role.USER, Role.ANALYST])('should block %s from manager-only route', (role) => {
    setCurrentUser(role);

    const result = TestBed.runInInjectionContext(() => roleGuard(route as never, {} as never));

    expect(result).not.toBe(true);
  });

  function setCurrentUser(role: Role): void {
    const user: User = {
      id: `usr-${role}`,
      name: role,
      email: `${role.toLowerCase()}@requestflow.com`,
      role,
      department: 'TI',
      birthDate: '1990-01-01',
      active: true,
      createdAt: new Date().toISOString(),
    };

    TestBed.inject(CurrentUserService).setCurrentUser(user);
  }
});
