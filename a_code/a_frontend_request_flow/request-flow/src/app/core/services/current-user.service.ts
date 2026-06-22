import { Injectable, computed, signal } from '@angular/core';
import { User } from '../models';

const CURRENT_USER_KEY = 'requestflow_current_user';

@Injectable({
  providedIn: 'root',
})
export class CurrentUserService {
  private readonly currentUserSignal = signal<User | null>(this.loadStoredUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  setCurrentUser(user: User): void {
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  getCurrentUser(): User | null {
    return this.currentUserSignal();
  }

  clearCurrentUser(): void {
    localStorage.removeItem(CURRENT_USER_KEY);
    this.currentUserSignal.set(null);
  }

  private loadStoredUser(): User | null {
    const storedUser = localStorage.getItem(CURRENT_USER_KEY);

    if (!storedUser) {
      return null;
    }

    try {
      return JSON.parse(storedUser) as User;
    } catch {
      localStorage.removeItem(CURRENT_USER_KEY);
      return null;
    }
  }
}
