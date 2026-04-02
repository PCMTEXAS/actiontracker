import { Component, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { Task, Status } from '../../models/task.model';

@Component({
  selector: 'app-task-row',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './task-row.component.html',
})
export class TaskRowComponent {
  protected readonly taskService = inject(TaskService);

  readonly task = input.required<Task>();
  readonly statusChanged = output<{ id: string; status: Status }>();
  readonly deleted = output<string>();

  protected readonly statuses: Status[] = ['NOT_STARTED', 'IN_PROGRESS', 'WAITING_ON', 'BLOCKED', 'COMPLETE'];

  protected onStatusChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as Status;
    this.statusChanged.emit({ id: this.task().id, status: value });
  }

  protected onDelete(): void {
    this.deleted.emit(this.task().id);
  }
}
