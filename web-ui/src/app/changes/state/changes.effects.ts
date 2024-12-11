import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';

import { catchError, concatMap, map } from 'rxjs/operators';
import { ChangesActions } from './changes.action.actions';
import { ChangeService } from '../change.service';
import { of } from 'rxjs';

@Injectable()
export class ChangesEffects {

  private readonly actions$ = inject(Actions);
  private readonly service = inject(ChangeService);
  
  loadChangess$ = createEffect(() => {
    return this.actions$.pipe(

      ofType(ChangesActions.loadChanges),
  
      concatMap(() => this.service.loadChanges().pipe(map(c => ChangesActions.loadChangesSuccess({changes: c} )))),
      catchError(e => of(ChangesActions.loadChangesFailure({error: e})))
    );
  });
}
