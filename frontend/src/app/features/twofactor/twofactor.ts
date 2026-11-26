import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { interval } from 'rxjs';
import { AuthService } from '../../core/services/auth';

/** Mismo plazo que el token pendiente de 2FA del backend. */
const PLAZO_SEGUNDOS = 5 * 60;

/** Segundo paso del login: el código de 6 cifras que llega por email. */
@Component({
  selector: 'app-twofactor',
  imports: [FormsModule, RouterModule, TranslateModule],
  templateUrl: './twofactor.html',
  styleUrl: './twofactor.css',
})
export class Twofactor implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected code = '';
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly restante = signal(PLAZO_SEGUNDOS);

  ngOnInit(): void {
    // Sin el token del primer paso no hay nada que verificar.
    if (!this.authService.getToken() || localStorage.getItem('twofactor') !== 'true') {
      this.router.navigate(['/login']);
      return;
    }
    interval(1000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.restante.update(s => s - 1);
      if (this.restante() <= 0) {
        this.authService.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  protected get tiempo(): string {
    const s = Math.max(this.restante(), 0);
    return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
  }

  protected onSubmit(): void {
    const code = this.code.replace(/\s/g, '');
    if (!/^\d{6}$/.test(code)) {
      this.error.set('TWOFACTOR.FORMAT');
      return;
    }
    this.error.set(null);
    this.enviando.set(true);
    this.authService.twofactor(code).subscribe({
      next: res => {
        localStorage.setItem('twofactor', 'false');
        this.authService.setToken(res.token);
        const returnUrl = sessionStorage.getItem('gogaming_return_url') || '/';
        sessionStorage.removeItem('gogaming_return_url');
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.enviando.set(false);
        this.error.set('TWOFACTOR.INVALID');
      },
    });
  }
}
