export type Priority = 'HIGH' | 'MEDIUM' | 'LOW';
export type Status = 'NOT_STARTED' | 'IN_PROGRESS' | 'WAITING_ON' | 'BLOCKED' | 'COMPLETE';
export type Recurrence = 'WEEKLY' | 'MONTHLY';

export interface Task {
  id: string;
  title: string;
  description: string;
  assigneeEmail: string;
  assigneeName: string;
  assignedByEmail: string;
  assignedByName: string;
  dueDate: string | null;
  priority: Priority;
  status: Status;
  source: string;
  sourceUrl: string | null;
  projectTag: string | null;
  isRecurring: boolean;
  recurrence: Recurrence | null;
  createdAt: string;
  updatedAt: string;
}

export interface TaskCreateRequest {
  title: string;
  description: string;
  assigneeEmail: string;
  assigneeName: string;
  dueDate: string | null;
  priority: Priority;
  status: Status;
  source: string;
  sourceUrl: string | null;
  projectTag: string | null;
  isRecurring: boolean;
  recurrence: Recurrence | null;
}

export interface TaskUpdateRequest {
  title?: string;
  description?: string;
  assigneeEmail?: string;
  assigneeName?: string;
  dueDate?: string | null;
  priority?: Priority;
  status?: Status;
  source?: string;
  sourceUrl?: string | null;
  projectTag?: string | null;
  isRecurring?: boolean;
  recurrence?: Recurrence | null;
}

export interface ExtractedTask {
  title: string;
  description: string;
  assignee: string;
  assigneeEmail: string | null;
  dueDate: string | null;
  priority: Priority;
  source: string;
  sourceUrl: string | null;
}

export interface TaskFilter {
  assigneeEmail?: string;
  status?: Status;
  priority?: Priority;
  dueDateFrom?: string;
  dueDateTo?: string;
  projectTag?: string;
  recurringOnly?: boolean;
}
