import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { Task } from '../../models/task.model';
import { Comment } from '../../models/comment.model';
import { TaskFormComponent } from '../task-form/task-form.component';
import { CommentInputComponent } from '../comment-input/comment-input.component';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, TaskFormComponent, CommentInputComponent],
  templateUrl: './task-detail.component.html',
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  protected readonly taskService = inject(TaskService);
  private readonly commentService = inject(CommentService);
  protected readonly authService = inject(AuthService);
  protected readonly task = signal<Task | null>(null);
  protected readonly comments = signal<Comment[]>([]);
  protected readonly loading = signal<boolean>(true);
  protected readonly error = signal<string | null>(null);
  protected readonly editMode = signal<boolean>(false);
  protected readonly commentsLoading = signal<boolean>(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Task ID not found');
      this.loading.set(false);
      return;
    }
    this.loadTask(id);
    this.loadComments(id);
  }

  private loadTask(id: string): void {
    this.loading.set(true);
    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.task.set(task);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set((err as Error).message || 'Failed to load task');
        this.loading.set(false);
      },
    });
  }

  private loadComments(id: string): void {
    this.commentsLoading.set(true);
    this.commentService.getComments(id).subscribe({
      next: (comments) => {
        this.comments.set(comments);
        this.commentsLoading.set(false);
      },
      error: () => this.commentsLoading.set(false),
    });
  }

  protected onTaskSaved(updated: Task): void {
    this.task.set(updated);
    this.editMode.set(false);
  }

  protected onCommentAdded(comment: Comment): void {
    this.comments.update((c) => [...c, comment]);
  }

  protected deleteTask(): void {
    const t = this.task();
    if (!t) return;
    if (!confirm('Delete this task?')) return;
    this.taskService.deleteTask(t.id).subscribe({
      next: () => {
        window.history.back();
      },
    });
  }

  protected formatDateTime(dateStr: string): string {
    return new Date(dateStr).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  }
}
