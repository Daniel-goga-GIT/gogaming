import { ComponentFixture, TestBed } from '@angular/core/testing';
import { testProviders } from '../../../testing/test-providers';
import { Register } from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: testProviders(),
    }).compileComponents();
    fixture = TestBed.createComponent(Register);
    fixture.detectChanges();
  });

  it('muestra el botón de Google que lleva al flujo OAuth del backend', () => {
    const enlace = fixture.nativeElement.querySelector('app-google-login-button a') as HTMLAnchorElement;
    expect(enlace.href).toContain('/oauth2/authorization/google');
  });
});
