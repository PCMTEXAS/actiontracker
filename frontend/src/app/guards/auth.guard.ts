import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { map, catchError, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { AppUser } from '../models/user.model';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const http = inject(HttpClient);
  const router = inject(Router);

  // Already loaded in this session — allow immediately
  if (authService.isAuthenticated()) {
    return true;
  }

  // First navigation after page load — verify session with backend
  return http.get<AppUser>('/api/users/me').pipe(
    map((user) => {
      authService.currentUser.set(user);
      return true;
    }),
    catchError(() => of(router.createUrlTree(['/login']))),
  );
};
