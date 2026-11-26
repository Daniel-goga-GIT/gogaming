import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environments';
import { configuration } from '../config/configuration';

/**
 * Añade el JWT a las peticiones a nuestra API, solo si hay token
 * (un "Bearer null" haría fallar los endpoints públicos).
 * Respeta la cabecera si el servicio ya la ha puesto.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = typeof localStorage === 'undefined' ? null : localStorage.getItem(configuration.KEY_TOKEN);
  if (!token || !req.url.startsWith(environment.apiUrl) || req.headers.has('Authorization')) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
