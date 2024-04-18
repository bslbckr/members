import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ResourceActions } from '../../actions/resource.actions';
import { map, concatMap, catchError, of, tap } from 'rxjs';
import { ResourceService } from '../resource.service';
import { Router } from '@angular/router';

@Injectable()
export class StartEffects {

    private readonly actions$ = inject(Actions);
    private readonly resourceSvc = inject(ResourceService);
    private readonly router = inject(Router);
    constructor() { }

    loadResources$ = createEffect(() => {
        return this.actions$.pipe(ofType(ResourceActions.loadResources),
            concatMap(_ => this.resourceSvc.getResourcesForUser()
                .pipe(
                    map(res => ResourceActions.loadResourcesSuccess({ data: res })))),
            catchError((err) => of(ResourceActions.loadResourcesFailure({ error: err }))));
    })

    selectMember$ = createEffect(() => {
        return this.actions$.pipe(ofType(ResourceActions.selectResource),
            tap(_ => this.router.navigate(["member"])),
            map(_ => { }));
    }, { dispatch: false });
}
