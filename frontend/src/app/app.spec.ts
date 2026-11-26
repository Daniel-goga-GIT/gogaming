import { TestBed } from '@angular/core/testing';
import { testProviders } from '../testing/test-providers';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: testProviders(),
    }).compileComponents();
  });

  it('monta la cabecera, el contenido y el pie', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelector('router-outlet')).toBeTruthy();
    expect(html.querySelector('app-header')).toBeTruthy();
    expect(html.querySelector('app-footer')).toBeTruthy();
  });
});
