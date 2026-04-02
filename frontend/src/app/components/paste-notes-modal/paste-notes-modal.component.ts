import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TaskService } from '../../services/task.service';
import { UserService } from '../../services/user.service';
import { ExtractedTask, Priority } from '../../models/task.model';
import { AppUser } from '../../models/user.model';

@Component({
  selector: 'app-paste-notes-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './paste-notes-modal.component.html',
})
export class PasteNotesModalComponent implements OnInit {
  protected readonly activeModal = inject(NgbActiveModal);
  private readonly taskService = inject(TaskService);
  private readonly userService = inject(UserService);

  protected readonly step = signal<'paste' | 'review'>('paste');
  protected readonly notes = signal<string>('');
  protected readonly extracting = signal<boolean>(false);
  protected readonly extractedTasks = signal<ExtractedTask[]>([]);
  protected readonly saving = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);
  protected readonly teamMembers = signal<AppUser[]>([]);

  protected readonly priorities: Priority[] = ['HIGH', 'MEDIUM', 'LOW'];

  ngOnInit(): void {
    this.userService.getTeamMembers().subscribe({
      next: (members) => this.teamMembers.set(members),
      error: () => {},
    });
  }

  protected extractTasks(): void {
    const text = this.notes().trim();
    if (!text) return;
    this.extracting.set(true);
    this.error.set(null);
    this.taskService.extractTasks(text).subscribe({
      next: (tasks) => {
        const matched = tasks.map((t) => ({
          ...t,
          assigneeEmail: t.assigneeEmail ?? this.fuzzyMatchAssignee(t.assignee)?.email ?? null,
        }));
        this.extractedTasks.set(matched);
        this.step.set('review');
        this.extracting.set(false);
      },
      error: (err) => {
        this.error.set((err as Error).message || 'Failed to extract tasks');
        this.extracting.set(false);
      },
    });
  }

  protected fuzzyMatchAssignee(assigneeName: string): AppUser | null {
    if (!assigneeName) return null;
    const tokens = assigneeName.toLowerCase().split(/\s+/);
    let bestMatch: AppUser | null = null;
    let bestScore = 0;
    for (const member of this.teamMembers()) {
      const memberTokens = member.name.toLowerCase().split(/\s+/);
      let score = 0;
      for (const token of tokens) {
        for (const mt of memberTokens) {
          if (mt.startsWith(token) || token.startsWith(mt)) {
            score++;
          }
        }
      }
      if (score > bestScore) {
        bestScore = score;
        bestMatch = member;
      }
    }
    return bestScore > 0 ? bestMatch : null;
  }

  protected updateExtractedTask(index: number, field: keyof ExtractedTask, value: unknown): void {
    const tasks = [...this.extractedTasks()];
    const task = { ...tasks[index] };
    // Type-safe update via type assertion
    (task as Record<string, unknown>)[field] = value;

    // If assigneeEmail updated, also try to update assignee name
    if (field === 'assigneeEmail') {
      const member = this.teamMembers().find((m) => m.email === value);
      if (member) {
        task.assignee = member.name;
      }
    }
    tasks[index] = task;
    this.extractedTasks.set(tasks);
  }

  protected addRow(): void {
    const blank: ExtractedTask = {
      title: '',
      description: '',
      assignee: '',
      assigneeEmail: null,
      dueDate: null,
      priority: 'MEDIUM',
      source: '',
      sourceUrl: null,
    };
    this.extractedTasks.set([...this.extractedTasks(), blank]);
  }

  protected removeRow(index: number): void {
    const tasks = [...this.extractedTasks()];
    tasks.splice(index, 1);
    this.extractedTasks.set(tasks);
  }

  protected confirmSave(): void {
    const tasks = this.extractedTasks();
    if (tasks.length === 0) return;
    this.saving.set(true);
    this.error.set(null);
    this.taskService.bulkCreateTasks(tasks).subscribe({
      next: (created) => {
        this.saving.set(false);
        this.activeModal.close(created);
      },
      error: (err) => {
        this.error.set((err as Error).message || 'Failed to save tasks');
        this.saving.set(false);
      },
    });
  }
}
