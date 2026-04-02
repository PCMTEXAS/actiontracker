import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TaskService } from './task.service';
import { Task, Priority, Status } from '../models/task.model';

const mockTask: Task = {
  id: 'uuid-1', title: 'Test Task', description: 'Desc',
  assigneeEmail: 'a@dc.com', assigneeName: 'Alice',
  assignedByEmail: 'b@dc.com', assignedByName: 'Bob',
  dueDate: '2026-12-31', priority: 'HIGH', status: 'NOT_STARTED',
  source: 'Test', sourceUrl: null, projectTag: null,
  isRecurring: false, recurrence: null,
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
};

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('getTasks should GET /api/tasks', () => {
    service.getTasks({}).subscribe(tasks => expect(tasks).toEqual([mockTask]));
    const req = httpMock.expectOne(r => r.url === '/api/tasks');
    expect(req.request.method).toBe('GET');
    req.flush([mockTask]);
  });

  it('createTask should POST /api/tasks', () => {
    const req_body = { title: 'New', description: '', assigneeEmail: 'a@dc.com', assigneeName: 'Alice', dueDate: null, priority: 'MEDIUM' as Priority, status: 'NOT_STARTED' as Status, source: '', sourceUrl: null, projectTag: null, isRecurring: false, recurrence: null };
    service.createTask(req_body).subscribe(t => expect(t.title).toBe('Test Task'));
    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('POST');
    req.flush(mockTask);
  });

  it('updateTask should PATCH /api/tasks/:id', () => {
    service.updateTask('uuid-1', { status: 'IN_PROGRESS' }).subscribe();
    const req = httpMock.expectOne('/api/tasks/uuid-1');
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockTask, status: 'IN_PROGRESS' });
  });

  it('deleteTask should DELETE /api/tasks/:id', () => {
    service.deleteTask('uuid-1').subscribe();
    const req = httpMock.expectOne('/api/tasks/uuid-1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('getPriorityBadgeClass should return correct classes', () => {
    expect(service.getPriorityBadgeClass('HIGH')).toContain('bg-danger');
    expect(service.getPriorityBadgeClass('MEDIUM')).toContain('bg-warning');
    expect(service.getPriorityBadgeClass('LOW')).toContain('bg-secondary');
  });

  it('getStatusBadgeClass should return correct classes', () => {
    expect(service.getStatusBadgeClass('BLOCKED')).toContain('bg-dark');
    expect(service.getStatusBadgeClass('COMPLETE')).toContain('bg-success');
  });

  it('isOverdue should return true for past due date when not complete', () => {
    const overdue = { ...mockTask, dueDate: '2020-01-01', status: 'NOT_STARTED' as Status };
    expect(service.isOverdue(overdue)).toBeTrue();
  });

  it('isOverdue should return false for COMPLETE tasks', () => {
    const done = { ...mockTask, dueDate: '2020-01-01', status: 'COMPLETE' as Status };
    expect(service.isOverdue(done)).toBeFalse();
  });
});
