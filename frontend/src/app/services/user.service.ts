import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AppUser } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  readonly teamMembers = signal<AppUser[]>([]);

  getTeamMembers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>('/api/users').pipe(
      tap((members) => this.teamMembers.set(members))
    );
  }
}
