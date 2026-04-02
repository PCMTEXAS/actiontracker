import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.component.html',
})
export class AppShellComponent implements OnInit {
  protected readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);

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
}
