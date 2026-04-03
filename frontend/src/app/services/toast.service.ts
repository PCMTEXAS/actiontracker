import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'danger' | 'info' | 'warning';

export interface Toast {
  id: string;
  message: string;
  type: ToastType;
  autoDismiss: boolean;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  show(message: string, type: ToastType = 'info', autoDismiss = true): string {
    const id = crypto.randomUUID();
    this.toasts.update((list) => [...list, { id, message, type, autoDismiss }]);
    if (autoDismiss) {
      setTimeout(() => this.dismiss(id), 4500);
    }
    return id;
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    // Errors stay until manually dismissed
    this.show(message, 'danger', false);
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  warning(message: string): void {
    this.show(message, 'warning');
  }

  dismiss(id: string): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  dismissAll(): void {
    this.toasts.set([]);
  }
}
