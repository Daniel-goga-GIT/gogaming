import { HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environments';
import { testProviders } from '../../../testing/test-providers';
import { OAuthCallback } from '../oauth-callback/oauth-callback';
import { Twofactor } from './twofactor';

describe('Login: 2FA y vuelta de Google', () => {
  let router: Router;
  let backend: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: testProviders() });
    router = TestBed.inject(Router);
    backend = TestBed.inject(HttpTestingController);
    spyOn(router, 'navigate').and.resolveTo(true);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  afterEach(() => {
    backend.verify();
    localStorage.clear();
  });

  it('sin el token del primer paso manda al login', () => {
    TestBed.createComponent(Twofactor).detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('valida el formato del código antes de llamar al backend y guarda el token completo', () => {
    localStorage.setItem('gogaming_token', 'pendiente');
    localStorage.setItem('twofactor', 'true');
    const fixture = TestBed.createComponent(Twofactor);
    fixture.detectChanges();
    const componente = fixture.componentInstance as unknown as { code: string; onSubmit(): void };

    componente.code = '12a';
    componente.onSubmit();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('TWOFACTOR.FORMAT');

    componente.code = '123 456';
    componente.onSubmit();
    const req = backend.expectOne(`${environment.apiUrl}/v1/twofactor`);
    expect(req.request.body).toEqual({ code: '123456' });
    req.flush({ token: 'completo' });

    expect(localStorage.getItem('gogaming_token')).toBe('completo');
    expect(localStorage.getItem('twofactor')).toBe('false');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
    fixture.destroy();
  });

  it('la vuelta de Google guarda el token del fragmento y lo borra de la URL', () => {
    history.replaceState(null, '', `${location.pathname}#token=jwt-de-google`);

    TestBed.createComponent(OAuthCallback).detectChanges();

    expect(localStorage.getItem('gogaming_token')).toBe('jwt-de-google');
    expect(localStorage.getItem('twofactor')).toBe('false');
    expect(location.hash).toBe('');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/', { replaceUrl: true });
  });

  it('si Google falla vuelve al login sin guardar nada', () => {
    history.replaceState(null, '', `${location.pathname}#error=oauth`);

    TestBed.createComponent(OAuthCallback).detectChanges();

    expect(localStorage.getItem('gogaming_token')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { replaceUrl: true });
  });
});
