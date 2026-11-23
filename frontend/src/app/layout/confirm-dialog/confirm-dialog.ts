import { Component, ElementRef, HostListener, effect, inject, viewChild } from '@angular/core';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (dialog.state(); as s) {
      <div class="fixed inset-0 z-[70] flex items-end sm:items-center justify-center p-4">
        <div class="backdrop absolute inset-0 bg-black/50 backdrop-blur-[2px]" (click)="dialog.close(false)" aria-hidden="true"></div>

        <div
          #panel
          class="panel relative w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="confirm-title"
          [attr.aria-describedby]="s.message ? 'confirm-message' : null"
        >
          <div class="flex items-start gap-4">
            <span
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full"
              [class]="s.variant === 'danger' ? 'bg-red-100 text-[#CC0000]' : 'bg-gray-100 text-gray-700'"
            >
              @if (s.variant === 'danger') {
                <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.87 12.14A2 2 0 0116.13 21H7.87a2 2 0 01-2-1.86L5 7m5 4v6m4-6v6M9 7V4a1 1 0 011-1h4a1 1 0 011 1v3M4 7h16" /></svg>
              } @else {
                <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M12 16v-4m0-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              }
            </span>
            <div class="min-w-0 pt-1">
              <h2 id="confirm-title" class="text-lg font-bold text-gray-900">{{ s.title }}</h2>
              @if (s.message) {
                <p id="confirm-message" class="mt-1.5 text-sm text-gray-600">{{ s.message }}</p>
              }
            </div>
          </div>

          <div class="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <button
              #cancelBtn
              type="button"
              class="rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 hover:bg-gray-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-gray-400 cursor-pointer"
              (click)="dialog.close(false)"
            >{{ s.cancelText }}</button>
            <button
              type="button"
              class="rounded-lg px-4 py-2.5 text-sm font-semibold text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 cursor-pointer"
              [class]="s.variant === 'danger'
                ? 'bg-[#CC0000] hover:bg-red-700 focus-visible:ring-red-600'
                : 'bg-[#111111] hover:bg-gray-800 focus-visible:ring-gray-900'"
              (click)="dialog.close(true)"
            >{{ s.confirmText }}</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .backdrop { animation: fade 150ms ease-out; }
    .panel { animation: pop 200ms cubic-bezier(.2,.8,.2,1); }
    @keyframes fade { from { opacity: 0; } }
    @keyframes pop { from { opacity: 0; transform: translateY(12px) scale(.97); } }
    @media (prefers-reduced-motion: reduce) { .backdrop, .panel { animation: none; } }
  `],
})
export class ConfirmDialog {
  protected dialog = inject(ConfirmDialogService);
  private cancelBtn = viewChild<ElementRef<HTMLButtonElement>>('cancelBtn');
  private panel = viewChild<ElementRef<HTMLElement>>('panel');
  private previouslyFocused: HTMLElement | null = null;

  constructor() {
    // Al abrir: foco en "Cancelar" (la opción segura) y bloqueo del scroll de fondo.
    effect(() => {
      const open = !!this.dialog.state();
      if (open) {
        this.previouslyFocused = document.activeElement as HTMLElement | null;
        document.body.style.overflow = 'hidden';
        queueMicrotask(() => this.cancelBtn()?.nativeElement.focus());
      } else {
        document.body.style.overflow = '';
        this.previouslyFocused?.focus?.();
        this.previouslyFocused = null;
      }
    });
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (!this.dialog.state()) return;

    if (event.key === 'Escape') {
      event.preventDefault();
      this.dialog.close(false);
      return;
    }

    // Mantiene el foco dentro del diálogo al tabular.
    if (event.key === 'Tab') {
      const focusables = this.panel()?.nativeElement.querySelectorAll<HTMLElement>('button');
      if (!focusables || focusables.length === 0) return;
      const first = focusables[0];
      const last = focusables[focusables.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    }
  }
}
