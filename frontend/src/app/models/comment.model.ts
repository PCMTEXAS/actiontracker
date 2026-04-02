export interface Comment {
  id: string;
  taskId: string;
  authorEmail: string;
  authorName: string;
  body: string;
  mentions: string[];
  createdAt: string;
}

export interface CommentCreateRequest {
  body: string;
}
