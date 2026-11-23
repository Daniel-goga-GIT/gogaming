import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {

  // Inyecta el servicio de autenticación.
  const authService = inject(AuthService);

  // Verifica si el usuario está autenticado.
  const token = authService.getToken();

  if (token && authService.getRole() === 'ROLE_ADMIN') {
    return true; 
  }

  // Redirige al login si no hay token.
  const router = inject(Router);
  return router.parseUrl('/');
  
};
