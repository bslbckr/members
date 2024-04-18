import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable } from 'rxjs';

import { StartEffects } from './start.effects';

describe('StartEffects', () => {
  let actions$: Observable<any>;
  let effects: StartEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        StartEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.inject(StartEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
