import { Component, OnInit, inject, input, output } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TaskService } from '../../services/task.service';
import { UserService } from '../../services/user.service';
import { Task, Priority, Status, Recurrence, TaskCreateRequest, TaskUpdateRequest } from '../../models/task.model';
import { AppUser } from '../../models/user.model';

interface TaskFormShape {
  title: FormControl<string>;
  description: FormControl<string>;
  assigneeEmail: FormControl<string>;
  assigneeName: FormControl<string>;
  dueDate: FormControl<string | null>;
  priority: FormControl<Priority>;
  status: FormControl<Status>;
  source: FormControl<string>;
  sourceUrl: FormControl<string | null>;
  projectTag: FormControl<string | null>;
  isRecurring: FormControl<boolean>;
  recurrence: FormControl<Recurrence | null>;
}

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-form.component.html',
})
export class TaskFormComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly userService = inject(UserService);

  // NgbActiveModal is optional — null when used inline, set when used as modal
  protected readonly activeModal = (() => {
    try {
      return inject(NgbActiveModal);
    } catch {
      return null;
    }
  })();

  readonly task = input<Task | null>(null);
  readonly saved = output<Task>();
  readonly cancelled = output<void>();

  protected saving = false;
  protected error: string | null = null;
  protected teamMembers: AppUser[] = [];

  protected readonly priorities: Priority[] = ['HIGH', 'MEDIUM', 'LOW'];
  protected readonly statuses: Status[] = ['NOT_STARTED', 'IN_PROGRESS', 'WAITING_ON', 'BLOCKED', 'COMPLETE'];
  protected readonly recurrences: Recurrence[] = ['WEEKLY', 'MONTHLY'];

  protected readonly form = new FormGroup<TaskFormShape>({
    title: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    description: new FormControl<string>('', { nonNullable: true }),
    assigneeEmail: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    assigneeName: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    dueDate: new FormControl<string | null>(null),
    priority: new FormControl<Priority>('MEDIUM', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    status: new FormControl<Status>('NOT_STARTED', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    source: new FormControl<string>('', { nonNullable: true }),
    sourceUrl: new FormControl<string | null>(null),
    projectTag: new FormControl<string | null>(null),
    isRecurring: new FormControl<boolean>(false, { nonNullable: true }),
    recurrence: new FormControl<Recurrence | null>(null),
  });

  ngOnInit(): void {
    this.userService.getTeamMembers().subscribe({
      next: (members) => (this.teamMembers = members),
      error: () => {},
    });

    const t = this.task();
    if (t) {
      this.form.patchValue({
        title: t.title,
        description: t.description,
        assigneeEmail: t.assigneeEmail,
        assigneeName: t.assigneeName,
        dueDate: t.dueDate,
        priority: t.priority,
        status: t.status,
        source: t.source,
        sourceUrl: t.sourceUrl,
        projectTag: t.projectTag,
        isRecurring: t.isRecurring,
        recurrence: t.recurrence,
      });
    }
  }

  protected onAssigneeSelect(event: Event): void {
    const email = (event.target as HTMLSelectElement).value;
    const member = this.teamMembers.find((m) => m.email === email);
    if (member) {
      this.form.controls.assigneeEmail.setValue(member.email);
      this.form.controls.assigneeName.setValue(member.name);
    }
  }

  protected get isRecurringValue(): boolean {
    return this.form.controls.isRecurring.value;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.error = null;
    const v = this.form.getRawValue();
    const t = this.task();

    if (t) {
      const req: TaskUpdateRequest = {
        title: v.title,
        description: v.description,
        assigneeEmail: v.assigneeEmail,
        assigneeName: v.assigneeName,
        dueDate: v.dueDate,
        priority: v.priority,
        status: v.status,
        source: v.source,
        sourceUrl: v.sourceUrl,
        projectTag: v.projectTag,
        isRecurring: v.isRecurring,
        recurrence: v.recurrence,
      };
      this.taskService.updateTask(t.id, req).subscribe({
        next: (updated) => {
          this.saving = false;
          this.saved.emit(updated);
          this.activeModal?.close(updated);
        },
        error: (err) => {
          this.saving = false;
          this.error = (err as Error).message || 'Failed to save task';
        },
      });
    } else {
      const req: TaskCreateRequest = {
        title: v.title,
        description: v.description,
        assigneeEmail: v.assigneeEmail,
        assigneeName: v.assigneeName,
        dueDate: v.dueDate,
        priority: v.priority,
        status: v.status,
        source: v.source,
        sourceUrl: v.sourceUrl,
        projectTag: v.projectTag,
        isRecurring: v.isRecurring,
        recurrence: v.recurrence,
      };
      this.taskService.createTask(req).subscribe({
        next: (created) => {
          this.saving = false;
          this.saved.emit(created);
          this.activeModal?.close(created);
        },
        error: (err) => {
          this.saving = false;
          this.error = (err as Error).message || 'Failed to create task';
        },
      });
    }
  }

  protected cancel(): void {
    this.cancelled.emit();
    this.activeModal?.dismiss();
  }
}
