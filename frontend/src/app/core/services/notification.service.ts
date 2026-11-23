import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastAction {
  label: string;
  /** Ruta a la que navegar al pulsar la acción (p. ej. '/cesta'). */
  route: string;
}

export interface Toast {
  id: number;
  type: ToastType;
  title: string;
  message?: string;
  image?: string;
  action?: ToastAction;
  duration: number;
}

export interface ToastOptions {
  message?: string;
  image?: string;
  action?: ToastAction;
  /** Milisegundos antes de cerrarse solo. 0 = no se cierra solo. */
  duration?: number;
}

/**
 * Avisos no bloqueantes (toasts) que sustituyen a window.alert().
 * Se muestran en <app-toast-container>, montado una sola vez en app.html.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  private readonly maxVisible = 4;
  private timers = new Map<number, ReturnType<typeof setTimeout>>();

  readonly toasts = signal<Toast[]>([]);

  success(title: string, options?: ToastOptions): number {
    return this.show('success', title, options);
  }

  error(title: string, options?: ToastOptions): number {
    return this.show('error', title, { duration: 6000, ...options });
  }

  warning(title: string, options?: ToastOptions): number {
    return this.show('warning', title, { duration: 5000, ...options });
  }

  info(title: string, options?: ToastOptions): number {
    return this.show('info', title, options);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  /** Pausa el cierre automático (al pasar el ratón por encima). */
  pause(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
  }

  /** Reanuda el cierre automático. */
  resume(id: number): void {
    const toast = this.toasts().find(t => t.id === id);
    if (toast && toast.duration > 0 && !this.timers.has(id)) {
      this.timers.set(id, setTimeout(() => this.dismiss(id), 2500));
    }
  }

  private show(type: ToastType, title: string, options: ToastOptions = {}): number {
    const id = this.nextId++;
    const toast: Toast = {
      id,
      type,
      title,
      message: options.message,
      image: options.image,
      action: options.action,
      duration: options.duration ?? 4000,
    };

    this.toasts.update(list => {
      const next = [...list, toast];
      // Si hay demasiados, se descartan los más antiguos.
      while (next.length > this.maxVisible) {
        const removed = next.shift();
        if (removed) this.pause(removed.id);
      }
      return next;
    });

    if (toast.duration > 0) {
      this.timers.set(id, setTimeout(() => this.dismiss(id), toast.duration));
    }
    return id;
  }
}
