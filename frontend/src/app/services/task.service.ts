import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Task,
  TaskCreateRequest,
  TaskUpdateRequest,
  TaskFilter,
  ExtractedTask,
  Priority,
  Status,
} from '../models/task.model';
import { Dashboard } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  getTasks(filter: TaskFilter): Observable<Task[]> {
    let params = new HttpParams();
    if (filter.assigneeEmail) params = params.set('assigneeEmail', filter.assigneeEmail);
    if (filter.status) params = params.set('status', filter.status);
    if (filter.priority) params = params.set('priority', filter.priority);
    if (filter.dueDateFrom) params = params.set('dueDateFrom', filter.dueDateFrom);
    if (filter.dueDateTo) params = params.set('dueDateTo', filter.dueDateTo);
    if (filter.projectTag) params = params.set('projectTag', filter.projectTag);
    if (filter.recurringOnly !== undefined) params = params.set('recurringOnly', String(filter.recurringOnly));
    return this.http.get<Task[]>('/api/tasks', { params });
  }

  getTaskById(id: string): Observable<Task> {
    return this.http.get<Task>(`/api/tasks/${id}`);
  }

  createTask(req: TaskCreateRequest): Observable<Task> {
    return this.http.post<Task>('/api/tasks', req);
  }

  updateTask(id: string, req: TaskUpdateRequest): Observable<Task> {
    return this.http.patch<Task>(`/api/tasks/${id}`, req);
  }

  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`/api/tasks/${id}`);
  }

  extractTasks(notes: string): Observable<ExtractedTask[]> {
    return this.http.post<ExtractedTask[]>('/api/tasks/extract', { notes });
  }

  bulkCreateTasks(items: ExtractedTask[]): Observable<Task[]> {
    return this.http.post<Task[]>('/api/tasks/bulk', { tasks: items });
  }

  exportCsv(filter: TaskFilter): Observable<Blob> {
    let params = new HttpParams();
    if (filter.assigneeEmail) params = params.set('assigneeEmail', filter.assigneeEmail);
    if (filter.status) params = params.set('status', filter.status);
    if (filter.priority) params = params.set('priority', filter.priority);
    if (filter.dueDateFrom) params = params.set('dueDateFrom', filter.dueDateFrom);
    if (filter.dueDateTo) params = params.set('dueDateTo', filter.dueDateTo);
    if (filter.projectTag) params = params.set('projectTag', filter.projectTag);
    if (filter.recurringOnly !== undefined) params = params.set('recurringOnly', String(filter.recurringOnly));
    return this.http.get('/api/tasks/export', { params, responseType: 'blob' });
  }

  getDashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>('/api/tasks/dashboard');
  }

  getPriorityBadgeClass(p: Priority): string {
    switch (p) {
      case 'HIGH':
        return 'badge bg-danger';
      case 'MEDIUM':
        return 'badge bg-warning text-dark';
      case 'LOW':
        return 'badge bg-secondary';
    }
  }

  getStatusBadgeClass(s: Status): string {
    switch (s) {
      case 'BLOCKED':
        return 'badge bg-dark';
      case 'WAITING_ON':
        return 'badge bg-info';
      case 'IN_PROGRESS':
        return 'badge bg-primary';
      case 'COMPLETE':
        return 'badge bg-success';
      case 'NOT_STARTED':
        return 'badge bg-secondary';
    }
  }

  isOverdue(task: Task): boolean {
    if (!task.dueDate || task.status === 'COMPLETE') return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(task.dueDate);
    return due < today;
  }
}
