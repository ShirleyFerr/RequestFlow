import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Role } from '../models';

export interface Profile {
  id: string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt?: string;
  birthDate?: string;
}

export interface ChangePasswordPayload {
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/profile`).pipe(map((profile) => this.mapProfile(profile)));
  }

  changePassword(payload: ChangePasswordPayload): Observable<Profile> {
    return this.http
      .put<Profile>(`${this.apiUrl}/profile/password`, payload)
      .pipe(map((profile) => this.mapProfile(profile)));
  }

  private mapProfile(profile: Profile): Profile {
    return {
      ...profile,
      id: String(profile.id),
    };
  }
}
