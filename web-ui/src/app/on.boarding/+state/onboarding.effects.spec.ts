import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable } from 'rxjs';

import { OnboardingEffects } from './onboarding.effects';

describe('OnboardingEffects', () => {
  let actions$: Observable<any>;
  let effects: OnboardingEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OnboardingEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.inject(OnboardingEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
