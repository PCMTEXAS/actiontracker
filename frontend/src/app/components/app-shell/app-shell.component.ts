import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../services/auth.service';
import { DashboardService } from '../../services/dashboard.service';
import { ToastService } from '../../services/toast.service';
import { ToastComponent } from '../toast/toast.component';
import { PasteNotesModalComponent } from '../paste-notes-modal/paste-notes-modal.component';
import { TaskFormComponent } from '../task-form/task-form.component';
import { UploadActionsModalComponent } from '../upload-actions-modal/upload-actions-modal.component';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent],
  templateUrl: './app-shell.component.html',
})
export class AppShellComponent implements OnInit {
  protected readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);
  private readonly modalService = inject(NgbModal);
  private readonly toastService = inject(ToastService);

  protected readonly overdueCount = signal<number>(0);
  protected readonly menuOpen = signal<boolean>(false);

  ngOnInit(): void {
    this.authService.loadCurrentUser();
    this.dashboardService.getDashboard().subscribe({
      next: (dash) => this.overdueCount.set(dash.overdueCount),
      error: () => this.overdueCount.set(0),
    });
  }

  protected toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  protected toggleDigest(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.authService.updatePreferences({ dailyDigestEnabled: checked });
  }

  protected openMeetingNotes(): void {
    const ref = this.modalService.open(PasteNotesModalComponent, {
      size: 'xl',
      scrollable: true,
      backdrop: 'static',
    });
    ref.result.then(
      (tasks: Task[]) => {
        if (tasks?.length) {
          this.toastService.show(`${tasks.length} action item${tasks.length > 1 ? 's' : ''} added`, 'success');
          window.dispatchEvent(new CustomEvent('tasks-updated'));
        }
      },
      () => {},
    );
  }

  protected openAddAction(): void {
    const ref = this.modalService.open(TaskFormComponent, {
      size: 'lg',
      scrollable: true,
      backdrop: 'static',
    });
    ref.result.then(
      (task: Task) => {
        if (task) {
          this.toastService.show('Action item added', 'success');
          window.dispatchEvent(new CustomEvent('tasks-updated'));
        }
      },
      () => {},
    );
  }

  protected openUploadActions(): void {
    const ref = this.modalService.open(UploadActionsModalComponent, {
      size: 'lg',
      scrollable: true,
      backdrop: 'static',
    });
    ref.result.then(
      (tasks: Task[]) => {
        if (tasks?.length) {
          this.toastService.show(`${tasks.length} action item${tasks.length > 1 ? 's' : ''} uploaded`, 'success');
          window.dispatchEvent(new CustomEvent('tasks-updated'));
        }
      },
      () => {},
    );
  }
}
