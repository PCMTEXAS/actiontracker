import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AppUser } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly currentUser = signal<AppUser | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isOwner = computed(() => this.currentUser()?.role === 'OWNER');

  loadCurrentUser(): void {
    this.http.get<AppUser>('/api/users/me').subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {
        this.currentUser.set(null);
        void this.router.navigate(['/login']);
      },
    });
  }

  logout(): void {
    this.http.get('/api/auth/logout', { responseType: 'text' }).subscribe({
      next: () => {
        this.currentUser.set(null);
        void this.router.navigate(['/login']);
      },
      error: () => {
        this.currentUser.set(null);
        void this.router.navigate(['/login']);
      },
    });
  }

  loginWithGoogle(): void {
    window.location.href = '/oauth2/authorization/google';
  }

  updatePreferences(prefs: { dailyDigestEnabled: boolean }): void {
    this.http.patch<AppUser>('/api/users/me/preferences', prefs).subscribe({
      next: (updated) => this.currentUser.set(updated),
      error: (err) => console.error('Failed to update preferences', err),
    });
  }

  getInitials(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length === 0 || parts[0] === '') return '?';
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
