import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { DashboardService } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ToastService } from '../../services/toast.service';
import { SkeletonComponent } from '../skeleton/skeleton.component';
import { Dashboard } from '../../models/dashboard.model';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe, SkeletonComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  protected readonly authService = inject(AuthService);
  protected readonly taskService = inject(TaskService);
  private readonly toastService = inject(ToastService);

  protected readonly dashboard = signal<Dashboard | null>(null);
  protected readonly loading = signal<boolean>(true);
  protected readonly error = signal<string | null>(null);
  protected readonly today = new Date();

  ngOnInit(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = (err as Error).message || 'Failed to load dashboard';
        this.error.set(msg);
        this.toastService.error(msg);
        this.loading.set(false);
      },
    });
  }

  protected trackById(_index: number, item: Task): string {
    return item.id;
  }

  protected formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
    });
  }
}
