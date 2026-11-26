import { TestBed } from '@angular/core/testing';
import { testProviders } from '../../../testing/test-providers';
import { CanActivateFn } from '@angular/router';

import { authGuardLogin } from './auth-login';

describe('authGuardLogin', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => authGuardLogin(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: testProviders() });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
