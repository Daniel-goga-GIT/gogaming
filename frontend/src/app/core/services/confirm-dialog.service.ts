import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  /** 'danger' pinta el botón de confirmar en rojo (borrar, vaciar...). */
  variant?: 'danger' | 'default';
}

interface ConfirmState extends Required<Omit<ConfirmOptions, 'message'>> {
  message?: string;
  resolve: (value: boolean) => void;
}

/**
 * Diálogo de confirmación accesible que sustituye a window.confirm().
 * Uso: if (!(await this.confirmDialog.confirm({ title: '¿Eliminar?' }))) return;
 */
@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  readonly state = signal<ConfirmState | null>(null);

  confirm(options: ConfirmOptions): Promise<boolean> {
    // Si ya había uno abierto, se resuelve como cancelado.
    this.state()?.resolve(false);

    return new Promise<boolean>(resolve => {
      this.state.set({
        title: options.title,
        message: options.message,
        confirmText: options.confirmText ?? 'Aceptar',
        cancelText: options.cancelText ?? 'Cancelar',
        variant: options.variant ?? 'default',
        resolve,
      });
    });
  }

  close(result: boolean): void {
    const current = this.state();
    if (!current) return;
    this.state.set(null);
    current.resolve(result);
  }
}
