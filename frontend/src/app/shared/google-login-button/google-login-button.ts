import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../environments/environments';

/**
 * Botón «Iniciar sesión con Google». Lleva al flujo OAuth2 del backend, que vuelve a
 * /oauth2/callback con el token. Es un enlace normal (no routerLink) porque sale de la app.
 */
@Component({
  selector: 'app-google-login-button',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <a [href]="url"
      class="w-full bg-white border border-gray-300 text-gray-800 font-bold py-3 px-4 rounded hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" class="w-5 h-5" aria-hidden="true">
        <path fill="#4285F4" d="M24 9.5c3.54 0 6 1.54 7.38 2.83l5.42-5.42C33.64 3.5 29.27 1.5 24 1.5 14.91 1.5 7.16 6.98 4.12 14.44l6.89 5.35C12.35 14.62 17.67 9.5 24 9.5z"/>
        <path fill="#34A853" d="M46.08 24.5c0-1.54-.14-3.02-.41-4.44H24v8.39h12.41c-.54 2.79-2.17 5.15-4.63 6.74l7.24 5.62C43.84 36.57 46.08 30.95 46.08 24.5z"/>
        <path fill="#FBBC05" d="M11.01 28.21A14.5 14.5 0 0 1 10.25 24c0-1.46.25-2.88.7-4.21l-6.89-5.35A22.42 22.42 0 0 0 1.92 24c0 3.6.86 7 2.39 10.01l6.7-5.8z"/>
        <path fill="#EA4335" d="M24 46.5c5.85 0 10.77-1.93 14.36-5.25l-7.24-5.62C29.33 37.34 26.86 38.5 24 38.5c-6.33 0-11.65-5.12-12.99-11.79l-6.89 5.35C7.16 41.02 14.91 46.5 24 46.5z"/>
      </svg>
      <span>{{ 'LOGIN.GOOGLE' | translate }}</span>
    </a>
  `,
})
export class GoogleLoginButton {
  /** apiUrl termina en /api; el flujo OAuth2 de Spring cuelga de la raíz del backend. */
  protected readonly url = `${environment.apiUrl.replace(/\/api\/?$/, '')}/oauth2/authorization/google`;

}
