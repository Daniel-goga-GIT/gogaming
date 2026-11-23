import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { inject } from '@angular/core';

export const authGuardLogin: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  // 1. Condición innegociable: ¿Existe el token?
  if (!token) {
    // Si no hay token, redirigimos al login (o raíz) de inmediato
    return router.parseUrl('/login');
  }

  const twofactor = localStorage.getItem('twofactor');

  if (token && twofactor === 'false') {
    return true;
  }

  if (token && twofactor === 'true') {
    return router.parseUrl('/login');
  }

  return router.parseUrl('/login');
};
