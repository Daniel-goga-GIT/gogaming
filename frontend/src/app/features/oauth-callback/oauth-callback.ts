import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth';
import { NotificationService } from '../../core/services/notification.service';

/**
 * Vuelta del login con Google: el backend redirige a /oauth2/callback#token=... (o #error=...).
 * El token va en el fragmento para que no viaje a ningún servidor; se guarda y se limpia la URL.
 */
@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="min-h-[50vh] flex flex-col items-center justify-center gap-4" aria-busy="true">
      <div class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-red-600"></div>
      <p class="text-sm text-gray-600">{{ 'OAUTH.PROCESSING' | translate }}</p>
    </div>
  `,
})
export class OAuthCallback implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private notify = inject(NotificationService);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    if (typeof window === 'undefined') return; // en el servidor (SSR) no hay fragmento

    const params = new URLSearchParams(window.location.hash.slice(1));
    const token = params.get('token');
    // Se borra el token de la barra de direcciones y del historial.
    history.replaceState(null, '', window.location.pathname);

    if (!token) {
      this.translate.get(['OAUTH.ERROR_TITLE', 'OAUTH.ERROR_MSG']).subscribe(t =>
        this.notify.error(t['OAUTH.ERROR_TITLE'], { message: t['OAUTH.ERROR_MSG'] }),
      );
      this.router.navigate(['/login'], { replaceUrl: true });
      return;
    }

    localStorage.setItem('twofactor', 'false');
    this.authService.setToken(token);
    const returnUrl = sessionStorage.getItem('gogaming_return_url') || '/';
    sessionStorage.removeItem('gogaming_return_url');
    this.router.navigateByUrl(returnUrl, { replaceUrl: true });
  }
}
