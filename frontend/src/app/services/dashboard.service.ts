import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Dashboard } from '../models/dashboard.model';
import { TaskService } from './task.service';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly taskService = inject(TaskService);

  getDashboard(): Observable<Dashboard> {
    return this.taskService.getDashboard();
  }
}
