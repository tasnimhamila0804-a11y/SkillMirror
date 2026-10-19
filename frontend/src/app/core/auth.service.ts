import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom, Observable, tap } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserSummary,
} from './models';
import { NotificationService } from './notification.service';

const TOKEN_KEY = 'skillmirror_token';

/** Authentication state: JWT storage, session lifecycle, account operations. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  readonly user = signal<UserSummary | null>(null);
  readonly loaded = signal(false);

  readonly isAuthenticated = computed(() => !!this.token);
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');
  readonly profileCompleted = computed(() => this.user()?.profileCompleted ?? false);

  private mePromise: Promise<UserSummary | null> | null = null;

  get token(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/api/auth/register', request)
      .pipe(tap((response) => this.setSession(response)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/api/auth/login', request)
      .pipe(tap((response) => this.setSession(response)));
  }

  logout(): void {
    if (this.token) {
      this.http.post('/api/auth/logout', {}).subscribe({ error: () => undefined });
    }
    this.clearSession();
    this.router.navigate(['/auth/login']);
  }

  /** Loads /api/auth/me once per session (used by guards and the shell). */
  ensureLoaded(): Promise<UserSummary | null> {
    if (this.user()) return Promise.resolve(this.user());
    if (!this.isAuthenticated) {
      this.loaded.set(true);
      return Promise.resolve(null);
    }
    if (!this.mePromise) {
      this.mePromise = firstValueFrom(this.http.get<UserSummary>('/api/auth/me'))
        .then((user) => {
          this.user.set(user);
          this.loaded.set(true);
          return user;
        })
        .catch(() => {
          this.clearSession();
          this.loaded.set(true);
          return null;
        });
    }
    return this.mePromise;
  }

  refreshMe(): void {
    this.mePromise = null;
    this.ensureLoaded();
  }

  setProfileCompleted(completed: boolean): void {
    this.user.update((user) => (user ? { ...user, profileCompleted: completed } : user));
  }

  setSession(response: AuthResponse): void {
    try {
      localStorage.setItem(TOKEN_KEY, response.token);
    } catch {
      // storage unavailable: session stays in memory for this tab
    }
    this.user.set(response.user);
    this.loaded.set(true);
    this.mePromise = Promise.resolve(response.user);
  }

  clearSession(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {
      // ignore
    }
    this.user.set(null);
    this.loaded.set(true);
    this.mePromise = null;
  }
}
