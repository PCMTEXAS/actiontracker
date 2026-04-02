import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment, CommentCreateRequest } from '../models/comment.model';

@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly http = inject(HttpClient);

  getComments(taskId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`/api/tasks/${taskId}/comments`);
  }

  addComment(taskId: string, body: string): Observable<Comment> {
    const req: CommentCreateRequest = { body };
    return this.http.post<Comment>(`/api/tasks/${taskId}/comments`, req);
  }
}
