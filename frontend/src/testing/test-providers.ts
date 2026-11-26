import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvironmentProviders, Provider } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { authInterceptor } from '../app/core/auth.interceptor';

/**
 * Proveedores comunes de los tests: HttpClient de pruebas (con el interceptor real),
 * router vacío y traducciones sin cargar (el pipe devuelve la clave).
 */
export function testProviders(): (Provider | EnvironmentProviders)[] {
  return [
    provideHttpClient(withInterceptors([authInterceptor])),
    provideHttpClientTesting(),
    provideRouter([]),
    provideTranslateService(),
  ];
}
