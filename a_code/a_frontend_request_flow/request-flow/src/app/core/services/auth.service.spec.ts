import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { CurrentUserService } from './current-user.service';
import { TokenService } from './token.service';
import { Role } from '../models';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let currentUserService: CurrentUserService;
  let httpMock: HttpTestingController;
  let tokenService: TokenService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: {
            navigate: vi.fn(),
          },
        },
      ],
    });
    service = TestBed.inject(AuthService);
    currentUserService = TestBed.inject(CurrentUserService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should login with valid backend credentials response', async () => {
    const loginPromise = firstValueFrom(
      service.login({ email: 'manager@requestflow.com', password: '123456' }),
    );
    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      email: 'manager@requestflow.com',
      password: '123456',
    });
    request.flush({
      token: 'jwt-token.manager',
      user: {
        id: 3,
        name: 'Manager',
        email: 'manager@requestflow.com',
        role: Role.MANAGER,
        active: true,
        createdAt: '2026-06-01T10:00:00',
      },
    });

    const result = await loginPromise;

    expect(result.user.role).toBe(Role.MANAGER);
    expect(result.user.id).toBe('3');
    expect(tokenService.getToken()).toContain('jwt-token.manager');
    expect(currentUserService.getCurrentUser()?.email).toBe('manager@requestflow.com');
  });

  it('should fail login with invalid credentials', async () => {
    const loginPromise = firstValueFrom(
      service.login({ email: 'manager@requestflow.com', password: 'wrong' }),
    );
    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    request.flush({ message: 'E-mail ou senha invalidos.' }, { status: 401, statusText: 'Unauthorized' });

    await expect(loginPromise).rejects.toThrow('E-mail ou senha invalidos.');
  });

  it('should clear token and user on logout', () => {
    tokenService.saveToken('fake-token');
    currentUserService.setCurrentUser({
      id: 'usr-test',
      name: 'Teste',
      email: 'teste@requestflow.com',
      role: Role.USER,
      department: 'TI',
      birthDate: '1990-01-01',
      active: true,
      createdAt: new Date().toISOString(),
    });

    service.logout();

    expect(tokenService.getToken()).toBeNull();
    expect(currentUserService.getCurrentUser()).toBeNull();
  });
});
