import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable } from 'rxjs';

import { ChangesEffects } from './changes.effects';

describe('ChangesEffects', () => {
  let actions$: Observable<any>;
  let effects: ChangesEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChangesEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.inject(ChangesEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
