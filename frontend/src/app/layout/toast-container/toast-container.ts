import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService, Toast } from '../../core/services/notification.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div
      class="pointer-events-none fixed inset-x-0 bottom-24 sm:bottom-auto sm:top-[6.75rem] z-[60] flex flex-col-reverse sm:flex-col items-center gap-3 px-4 sm:items-end sm:pr-6"
      aria-live="polite"
      aria-atomic="false"
    >
      @for (toast of notifications.toasts(); track toast.id) {
        <div
          class="toast pointer-events-auto w-full max-w-sm overflow-hidden rounded-xl bg-white shadow-xl ring-1 ring-black/5"
          [attr.role]="toast.type === 'error' ? 'alert' : 'status'"
          (mouseenter)="notifications.pause(toast.id)"
          (mouseleave)="notifications.resume(toast.id)"
        >
          <div class="flex items-start gap-3 p-4">
            @if (toast.image) {
              <img [src]="toast.image" alt="" class="h-14 w-14 shrink-0 rounded-lg bg-gray-50 object-contain p-1" />
            } @else {
              <span class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full" [class]="iconBg(toast)">
                @switch (toast.type) {
                  @case ('success') {
                    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                  }
                  @case ('error') {
                    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                  }
                  @case ('warning') {
                    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 4h.01" /></svg>
                  }
                  @default {
                    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M12 16v-4m0-4h.01" /></svg>
                  }
                }
              </span>
            }

            <div class="min-w-0 flex-1">
              <p class="text-sm font-semibold text-gray-900 flex items-center gap-1.5">
                @if (toast.image && toast.type === 'success') {
                  <svg class="h-4 w-4 text-green-600 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                }
                {{ toast.title }}
              </p>
              @if (toast.message) {
                <p class="mt-0.5 text-sm text-gray-600 line-clamp-2">{{ toast.message }}</p>
              }
              @if (toast.action) {
                <button
                  type="button"
                  class="mt-2 inline-flex items-center gap-1 text-sm font-semibold text-[#CC0000] hover:text-red-800 cursor-pointer"
                  (click)="runAction(toast)"
                >
                  {{ toast.action.label }}
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
                </button>
              }
            </div>

            <button
              type="button"
              class="-m-1 shrink-0 rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 cursor-pointer"
              (click)="notifications.dismiss(toast.id)"
              aria-label="Cerrar aviso"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>

          @if (toast.duration > 0) {
            <div class="h-1" [class]="barBg(toast)">
              <div class="toast-progress h-full bg-current opacity-60" [style.animation-duration.ms]="toast.duration"></div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .toast { animation: toast-in 220ms cubic-bezier(.2,.8,.2,1); }
    .toast:hover .toast-progress { animation-play-state: paused; }
    .toast-progress { width: 100%; transform-origin: left; animation-name: toast-progress; animation-timing-function: linear; animation-fill-mode: forwards; }
    @keyframes toast-in { from { opacity: 0; transform: translateY(8px) scale(.98); } to { opacity: 1; transform: none; } }
    @media (min-width: 640px) { @keyframes toast-in { from { opacity: 0; transform: translateY(-8px) scale(.98); } to { opacity: 1; transform: none; } } }
    @keyframes toast-progress { from { transform: scaleX(1); } to { transform: scaleX(0); } }
    @media (prefers-reduced-motion: reduce) { .toast { animation: none; } }
  `],
})
export class ToastContainer {
  protected notifications = inject(NotificationService);
  private router = inject(Router);

  iconBg(toast: Toast): string {
    return {
      success: 'bg-green-100 text-green-700',
      error: 'bg-red-100 text-[#CC0000]',
      warning: 'bg-amber-100 text-amber-700',
      info: 'bg-gray-100 text-gray-700',
    }[toast.type];
  }

  barBg(toast: Toast): string {
    return {
      success: 'bg-green-50 text-green-600',
      error: 'bg-red-50 text-[#CC0000]',
      warning: 'bg-amber-50 text-amber-500',
      info: 'bg-gray-100 text-gray-500',
    }[toast.type];
  }

  runAction(toast: Toast): void {
    if (toast.action) {
      this.router.navigateByUrl(toast.action.route);
    }
    this.notifications.dismiss(toast.id);
  }
}
