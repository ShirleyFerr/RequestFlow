import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Role, User } from '../models';

export interface UserCreatePayload {
  name: string;
  email: string;
  role: Role;
  birthDate: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listUsers(filters: { role?: Role; active?: boolean } = {}): Observable<User[]> {
    let params = new HttpParams();

    if (filters.role) {
      params = params.set('role', filters.role);
    }

    if (filters.active !== undefined) {
      params = params.set('active', String(filters.active));
    }

    return this.http
      .get<User[]>(`${this.apiUrl}/users`, { params })
      .pipe(map((users) => users.map((user) => this.mapUser(user))));
  }

  listActiveAnalysts(): Observable<User[]> {
    return this.http
      .get<User[]>(`${this.apiUrl}/users/analysts`)
      .pipe(map((users) => users.map((user) => this.mapUser(user))));
  }

  createUser(payload: UserCreatePayload): Observable<User> {
    return this.http
      .post<User>(`${this.apiUrl}/users`, payload)
      .pipe(map((user) => this.mapUser(user)));
  }

  updateActive(userId: string, active: boolean): Observable<User> {
    return this.http
      .patch<User>(`${this.apiUrl}/users/${userId}/active`, { active })
      .pipe(map((user) => this.mapUser(user)));
  }

  private mapUser(user: User): User {
    return {
      ...user,
      id: String(user.id),
    };
  }
}
