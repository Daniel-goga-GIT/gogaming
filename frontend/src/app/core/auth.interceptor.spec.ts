import { HttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environments';
import { testProviders } from '../../testing/test-providers';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: testProviders() });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    localStorage.clear();
  });

  it('añade el token a las peticiones a la API', () => {
    localStorage.setItem('gogaming_token', 'abc');
    http.get(`${environment.apiUrl}/v1/users/me`).subscribe();
    expect(backend.expectOne(`${environment.apiUrl}/v1/users/me`).request.headers.get('Authorization')).toBe('Bearer abc');
  });

  it('no añade nada si no hay token (un "Bearer null" rompería los endpoints públicos)', () => {
    http.get(`${environment.apiUrl}/v1/productos/home`).subscribe();
    expect(backend.expectOne(`${environment.apiUrl}/v1/productos/home`).request.headers.has('Authorization')).toBeFalse();
  });

  it('no envía el token a otros dominios', () => {
    localStorage.setItem('gogaming_token', 'abc');
    http.get('https://otro-sitio.example/datos').subscribe();
    expect(backend.expectOne('https://otro-sitio.example/datos').request.headers.has('Authorization')).toBeFalse();
  });
});
