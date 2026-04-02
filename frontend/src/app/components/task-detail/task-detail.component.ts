import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { Task } from '../../models/task.model';
import { Comment } from '../../models/comment.model';
import { ActivityItem } from '../../models/dashboard.model';
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
  protected readonly activity = signal<ActivityItem[]>([]);
  protected readonly loading = signal<boolean>(true);
  protected readonly error = signal<string | null>(null);
  protected readonly editMode = signal<boolean>(false);
  protected readonly commentsLoading = signal<boolean>(false);
  protected readonly activityLoading = signal<boolean>(false);
  protected readonly activeTab = signal<'comments' | 'activity'>('comments');

  private taskId = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Task ID not found');
      this.loading.set(false);
      return;
    }
    this.taskId = id;
    this.loadTask(id);
    this.loadComments(id);
    this.loadActivity(id);
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

  private loadActivity(id: string): void {
    this.activityLoading.set(true);
    this.commentService.getTaskActivity(id).subscribe({
      next: (items) => {
        this.activity.set(items);
        this.activityLoading.set(false);
      },
      error: () => this.activityLoading.set(false),
    });
  }

  protected onTaskSaved(updated: Task): void {
    this.task.set(updated);
    this.editMode.set(false);
    this.loadActivity(this.taskId);
  }

  protected onCommentAdded(comment: Comment): void {
    this.comments.update((c) => [...c, comment]);
    this.loadActivity(this.taskId);
  }

  protected deleteComment(commentId: string): void {
    if (!confirm('Delete this comment?')) return;
    this.commentService.deleteComment(this.taskId, commentId).subscribe({
      next: () => {
        this.comments.update((c) => c.filter((cm) => cm.id !== commentId));
        this.loadActivity(this.taskId);
      },
    });
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

  protected activityIcon(eventType: string): string {
    switch (eventType) {
      case 'TASK_CREATED':    return '✚';
      case 'STATUS_CHANGED':  return '↔';
      case 'REASSIGNED':      return '👤';
      case 'DUE_DATE_CHANGED': return '📅';
      case 'COMMENT_ADDED':   return '💬';
      default:                return '•';
    }
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
