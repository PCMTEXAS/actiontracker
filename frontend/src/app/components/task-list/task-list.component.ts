import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbModal, NgbPagination } from '@ng-bootstrap/ng-bootstrap';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { Task, TaskFilter, Status } from '../../models/task.model';
import { AppUser } from '../../models/user.model';
import { TaskKanbanComponent } from '../task-kanban/task-kanban.component';
import { PasteNotesModalComponent } from '../paste-notes-modal/paste-notes-modal.component';
import { TaskFormComponent } from '../task-form/task-form.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    NgbPagination,
    TaskKanbanComponent,
  ],
  templateUrl: './task-list.component.html',
})
export class TaskListComponent implements OnInit {
  protected readonly taskService = inject(TaskService);
  protected readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly modalService = inject(NgbModal);

  protected readonly tasks = signal<Task[]>([]);
  protected readonly loading = signal<boolean>(true);
  protected readonly error = signal<string | null>(null);
  protected readonly selectedIds = signal<Set<string>>(new Set());
  protected readonly viewMode = signal<'list' | 'kanban'>('list');
  protected readonly page = signal<number>(1);
  protected readonly pageSize = signal<number>(20);
  protected readonly sortField = signal<string>('dueDate');
  protected readonly sortAsc = signal<boolean>(true);
  protected readonly teamMembers = signal<AppUser[]>([]);

  // Filter signals
  protected readonly filterAssigneeEmail = signal<string>('');
  protected readonly filterStatus = signal<string>('');
  protected readonly filterPriority = signal<string>('');
  protected readonly filterDueDateFrom = signal<string>('');
  protected readonly filterDueDateTo = signal<string>('');
  protected readonly filterProjectTag = signal<string>('');
  protected readonly filterRecurringOnly = signal<boolean>(false);

  protected readonly filter = computed<TaskFilter>(() => {
    const f: TaskFilter = {};
    if (this.filterAssigneeEmail()) f.assigneeEmail = this.filterAssigneeEmail();
    if (this.filterStatus()) f.status = this.filterStatus() as Status;
    if (this.filterPriority()) f.priority = this.filterPriority() as Task['priority'];
    if (this.filterDueDateFrom()) f.dueDateFrom = this.filterDueDateFrom();
    if (this.filterDueDateTo()) f.dueDateTo = this.filterDueDateTo();
    if (this.filterProjectTag()) f.projectTag = this.filterProjectTag();
    if (this.filterRecurringOnly()) f.recurringOnly = true;
    return f;
  });

  protected readonly sortedTasks = computed<Task[]>(() => {
    const list = [...this.tasks()];
    const field = this.sortField();
    const asc = this.sortAsc();
    list.sort((a, b) => {
      let av: string | null = null;
      let bv: string | null = null;
      if (field === 'dueDate') {
        av = a.dueDate;
        bv = b.dueDate;
      } else if (field === 'priority') {
        const order: Record<string, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
        av = String(order[a.priority] ?? 1);
        bv = String(order[b.priority] ?? 1);
      } else if (field === 'title') {
        av = a.title;
        bv = b.title;
      } else if (field === 'assignee') {
        av = a.assigneeName;
        bv = b.assigneeName;
      } else if (field === 'status') {
        av = a.status;
        bv = b.status;
      }
      if (av === null && bv === null) return 0;
      if (av === null) return asc ? 1 : -1;
      if (bv === null) return asc ? -1 : 1;
      const cmp = av.localeCompare(bv);
      return asc ? cmp : -cmp;
    });
    return list;
  });

  protected readonly pagedTasks = computed<Task[]>(() => {
    const start = (this.page() - 1) * this.pageSize();
    return this.sortedTasks().slice(start, start + this.pageSize());
  });

  protected readonly allSelected = computed<boolean>(
    () => this.tasks().length > 0 && this.selectedIds().size === this.tasks().length
  );

  protected readonly statuses: Status[] = ['NOT_STARTED', 'IN_PROGRESS', 'WAITING_ON', 'BLOCKED', 'COMPLETE'];

  ngOnInit(): void {
    this.loadTasks();
    this.userService.getTeamMembers().subscribe({
      next: (members) => this.teamMembers.set(members),
      error: () => {},
    });
  }

  protected loadTasks(): void {
    this.loading.set(true);
    this.error.set(null);
    this.taskService.getTasks(this.filter()).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.loading.set(false);
        this.page.set(1);
      },
      error: (err) => {
        this.error.set((err as Error).message || 'Failed to load tasks');
        this.loading.set(false);
      },
    });
  }

  protected applyFilters(): void {
    this.loadTasks();
  }

  protected clearFilters(): void {
    this.filterAssigneeEmail.set('');
    this.filterStatus.set('');
    this.filterPriority.set('');
    this.filterDueDateFrom.set('');
    this.filterDueDateTo.set('');
    this.filterProjectTag.set('');
    this.filterRecurringOnly.set(false);
    this.loadTasks();
  }

  protected openPasteModal(): void {
    const ref = this.modalService.open(PasteNotesModalComponent, {
      size: 'xl',
      scrollable: true,
    });
    ref.closed.subscribe(() => this.loadTasks());
  }

  protected openCreateModal(): void {
    const ref = this.modalService.open(TaskFormComponent, {
      size: 'lg',
      scrollable: true,
    });
    ref.closed.subscribe(() => this.loadTasks());
  }

  protected toggleSort(field: string): void {
    if (this.sortField() === field) {
      this.sortAsc.set(!this.sortAsc());
    } else {
      this.sortField.set(field);
      this.sortAsc.set(true);
    }
  }

  protected toggleSelect(id: string): void {
    const set = new Set(this.selectedIds());
    if (set.has(id)) {
      set.delete(id);
    } else {
      set.add(id);
    }
    this.selectedIds.set(set);
  }

  protected toggleSelectAll(): void {
    if (this.allSelected()) {
      this.selectedIds.set(new Set());
    } else {
      this.selectedIds.set(new Set(this.tasks().map((t) => t.id)));
    }
  }

  protected clearSelection(): void {
    this.selectedIds.set(new Set());
  }

  protected bulkDelete(): void {
    const ids = [...this.selectedIds()];
    if (ids.length === 0) return;
    if (!confirm(`Delete ${ids.length} selected task(s)?`)) return;
    let remaining = ids.length;
    for (const id of ids) {
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          remaining--;
          if (remaining === 0) {
            this.selectedIds.set(new Set());
            this.loadTasks();
          }
        },
      });
    }
  }

  protected bulkStatusChange(status: Status): void {
    const ids = [...this.selectedIds()];
    if (ids.length === 0) return;
    let remaining = ids.length;
    for (const id of ids) {
      this.taskService.updateTask(id, { status }).subscribe({
        next: () => {
          remaining--;
          if (remaining === 0) {
            this.selectedIds.set(new Set());
            this.loadTasks();
          }
        },
      });
    }
  }

  protected onStatusChange(id: string, status: Status): void {
    this.taskService.updateTask(id, { status }).subscribe({
      next: (updated) => {
        this.tasks.update((tasks) =>
          tasks.map((t) => (t.id === id ? updated : t))
        );
      },
    });
  }

  protected deleteTask(id: string): void {
    if (!confirm('Delete this task?')) return;
    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.tasks.update((tasks) => tasks.filter((t) => t.id !== id));
        const set = new Set(this.selectedIds());
        set.delete(id);
        this.selectedIds.set(set);
      },
    });
  }

  protected exportCsv(): void {
    this.taskService.exportCsv(this.filter()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'tasks.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
    });
  }

  protected sortIcon(field: string): string {
    if (this.sortField() !== field) return '↕';
    return this.sortAsc() ? '↑' : '↓';
  }

  protected isSelected(id: string): boolean {
    return this.selectedIds().has(id);
  }
}
