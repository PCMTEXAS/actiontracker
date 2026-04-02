import { Task } from './task.model';

export interface ActivityItem {
  id: string;
  taskId: string;
  actorEmail: string;
  actorName: string;
  eventType: string;
  detail: string | null;
  createdAt: string;
}

export interface Dashboard {
  myOpenTasks: Task[];
  overdueCount: number;
  overdueTasks: Task[];
  waitingOnOthers: Task[];
  assignedByMe: Task[];
  recentActivity: ActivityItem[];
}
