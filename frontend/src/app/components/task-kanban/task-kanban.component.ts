import { Component, inject, input, output, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { TaskService } from '../../services/task.service';
import { Task, Status } from '../../models/task.model';

@Component({
  selector: 'app-task-kanban',
  standalone: true,
  imports: [RouterLink, DragDropModule],
  templateUrl: './task-kanban.component.html',
  styles: [`
    .kanban-column { min-width: 240px; max-width: 280px; }
    .kanban-card { cursor: grab; }
    .kanban-card:active { cursor: grabbing; }
    .cdk-drag-preview { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
    .cdk-drag-placeholder { opacity: 0.3; }
    .cdk-drag-animating { transition: transform 250ms cubic-bezier(0, 0, 0.2, 1); }
    .cdk-drop-list-dragging .kanban-card:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
  `],
})
export class TaskKanbanComponent {
  protected readonly taskService = inject(TaskService);

  readonly tasks = input<Task[]>([]);
  readonly taskMoved = output<{ id: string; status: Status }>();

  protected readonly STATUSES: Status[] = ['NOT_STARTED', 'IN_PROGRESS', 'WAITING_ON', 'BLOCKED', 'COMPLETE'];
  protected readonly DROP_LIST_IDS = this.STATUSES.map((s) => 'kanban-' + s);

  protected readonly STATUS_LABELS: Record<Status, string> = {
    NOT_STARTED: 'Not Started',
    IN_PROGRESS: 'In Progress',
    WAITING_ON: 'Waiting On',
    BLOCKED: 'Blocked',
    COMPLETE: 'Complete',
  };

  protected readonly STATUS_HEADER_CLASS: Record<Status, string> = {
    NOT_STARTED: 'bg-secondary',
    IN_PROGRESS: 'bg-primary',
    WAITING_ON: 'bg-info',
    BLOCKED: 'bg-dark',
    COMPLETE: 'bg-success',
  };

  protected readonly notStarted = computed(() => this.tasks().filter((t) => t.status === 'NOT_STARTED'));
  protected readonly inProgress = computed(() => this.tasks().filter((t) => t.status === 'IN_PROGRESS'));
  protected readonly waitingOn = computed(() => this.tasks().filter((t) => t.status === 'WAITING_ON'));
  protected readonly blocked = computed(() => this.tasks().filter((t) => t.status === 'BLOCKED'));
  protected readonly complete = computed(() => this.tasks().filter((t) => t.status === 'COMPLETE'));

  protected getColumnTasks(status: Status): Task[] {
    switch (status) {
      case 'NOT_STARTED': return this.notStarted();
      case 'IN_PROGRESS': return this.inProgress();
      case 'WAITING_ON': return this.waitingOn();
      case 'BLOCKED': return this.blocked();
      case 'COMPLETE': return this.complete();
    }
  }

  protected drop(event: CdkDragDrop<Task[]>, targetStatus: Status): void {
    if (event.previousContainer !== event.container) {
      const task = event.previousContainer.data[event.previousIndex] as Task;
      this.taskService.updateTask(task.id, { status: targetStatus }).subscribe();
      this.taskMoved.emit({ id: task.id, status: targetStatus });
    } else {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    }
  }

  protected getInitials(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length === 0 || parts[0] === '') return '?';
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
