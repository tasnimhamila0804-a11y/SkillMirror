import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  kind: 'success' | 'error' | 'info';
  message: string;
}

/** Lightweight toast notifications used across the app. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly toasts = signal<Toast[]>([]);
  private counter = 0;

  success(message: string): void {
    this.push('success', message);
  }

  error(message: string): void {
    this.push('error', message);
  }

  info(message: string): void {
    this.push('info', message);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  private push(kind: Toast['kind'], message: string): void {
    const id = ++this.counter;
    this.toasts.update((list) => [...list, { id, kind, message }]);
    const timeout = kind === 'error' ? 7000 : 4200;
    setTimeout(() => this.dismiss(id), timeout);
  }
}
