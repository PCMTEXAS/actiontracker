import { Component, inject, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TaskService } from '../../services/task.service';
import { ExtractedTask } from '../../models/task.model';

@Component({
  selector: 'app-upload-actions-modal',
  standalone: true,
  imports: [],
  templateUrl: './upload-actions-modal.component.html',
})
export class UploadActionsModalComponent {
  protected readonly activeModal = inject(NgbActiveModal);
  private readonly taskService = inject(TaskService);

  protected readonly fileName = signal<string | null>(null);
  protected readonly preview = signal<ExtractedTask[]>([]);
  protected readonly saving = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);
  protected readonly saved = signal<number>(0);

  protected onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.fileName.set(file.name);
    this.error.set(null);

    const reader = new FileReader();
    reader.onload = (e) => {
      const text = (e.target?.result as string) ?? '';
      try {
        const tasks = this.parseCsv(text);
        this.preview.set(tasks);
      } catch (err) {
        this.error.set('Could not parse CSV. Please check the format.');
      }
    };
    reader.readAsText(file);
  }

  private parseCsv(text: string): ExtractedTask[] {
    const lines = text.split(/\r?\n/).filter((l) => l.trim().length > 0);
    if (lines.length < 2) throw new Error('Empty CSV');

    // Normalize headers
    const headers = lines[0].split(',').map((h) => h.trim().toLowerCase().replace(/\s+/g, '_'));

    return lines.slice(1).map((line) => {
      const cols = this.splitCsvLine(line);
      const get = (key: string) => {
        const idx = headers.indexOf(key);
        return idx >= 0 ? (cols[idx] ?? '').trim() : '';
      };

      const priority = (['HIGH', 'MEDIUM', 'LOW'].includes(get('priority').toUpperCase())
        ? get('priority').toUpperCase()
        : 'MEDIUM') as 'HIGH' | 'MEDIUM' | 'LOW';

      return {
        title: get('title') || get('action') || get('task') || '',
        description: get('description') || get('notes') || '',
        assignee: get('assignee') || get('owner') || get('assigned_to') || '',
        assigneeEmail: get('assignee_email') || get('email') || null,
        dueDate: get('due_date') || get('due') || null,
        priority,
        source: get('source') || 'CSV Upload',
        sourceUrl: null,
      } satisfies ExtractedTask;
    }).filter((t) => t.title.length > 0);
  }

  private splitCsvLine(line: string): string[] {
    const result: string[] = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (ch === '"') {
        inQuotes = !inQuotes;
      } else if (ch === ',' && !inQuotes) {
        result.push(current);
        current = '';
      } else {
        current += ch;
      }
    }
    result.push(current);
    return result;
  }

  protected submit(): void {
    const tasks = this.preview();
    if (tasks.length === 0) return;
    this.saving.set(true);
    this.error.set(null);
    this.taskService.bulkCreateTasks(tasks).subscribe({
      next: (created) => {
        this.saving.set(false);
        this.activeModal.close(created);
      },
      error: () => {
        this.error.set('Failed to save tasks. Please try again.');
        this.saving.set(false);
      },
    });
  }

  protected downloadTemplate(): void {
    const csv = [
      'title,description,assignee,assignee_email,due_date,priority,source',
      '"Review Q2 pipeline","Check all deals in HubSpot","Patrick","patrick@pcmtexas.com","2026-04-15","HIGH","Pipeline Review"',
      '"Send follow-up email","Follow up with prospect","Jane","jane@pcmtexas.com","2026-04-12","MEDIUM","Sales Call"',
    ].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'action-items-template.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
