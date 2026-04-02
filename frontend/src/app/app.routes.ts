import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/app-shell/app-shell.component').then(
        (m) => m.AppShellComponent
      ),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./components/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'tasks',
        loadComponent: () =>
          import('./components/task-list/task-list.component').then(
            (m) => m.TaskListComponent
          ),
      },
      {
        path: 'tasks/:id',
        loadComponent: () =>
          import('./components/task-detail/task-detail.component').then(
            (m) => m.TaskDetailComponent
          ),
      },
    ],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./components/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },
];
