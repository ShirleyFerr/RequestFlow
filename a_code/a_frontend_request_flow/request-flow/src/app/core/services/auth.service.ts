import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models';
import { CurrentUserService } from './current-user.service';
import { TokenService } from './token.service';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface LoginResult {
  token: string;
  tokenType?: string;
  user: User;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly currentUserService = inject(CurrentUserService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);
  private readonly apiUrl = environment.apiUrl;

  login(credentials: LoginCredentials): Observable<LoginResult> {
    return this.http.post<LoginResult>(`${this.apiUrl}/auth/login`, credentials).pipe(
      map((loginResult) => {
        const user = this.mapUser(loginResult.user);
        this.tokenService.saveToken(loginResult.token);
        this.currentUserService.setCurrentUser(user);
        return { ...loginResult, user };
      }),
      catchError((error: HttpErrorResponse) =>
        throwError(() => new Error(error.error?.message ?? 'E-mail ou senha invalidos.')),
      ),
    );
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/auth/me`).pipe(
      map((user) => {
        const mappedUser = this.mapUser(user);
        this.currentUserService.setCurrentUser(mappedUser);
        return mappedUser;
      }),
    );
  }

  logout(): void {
    this.tokenService.removeToken();
    this.currentUserService.clearCurrentUser();
    void this.router.navigate(['/auth/login']);
  }

  private mapUser(user: User): User {
    return {
      ...user,
      id: String(user.id),
    };
  }
}
