import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, exhaustMap, map } from 'rxjs/operators';
import { onBoardingActions } from '../../actions/actions';
import { OnBoardingService } from '../on-boarding.service';


@Injectable()
export class OnboardingEffects {

    private readonly actions$ = inject(Actions);
    private readonly service = inject(OnBoardingService);

    onboardingOnboardings$ = createEffect(() => {
        return this.actions$.pipe(

            ofType(onBoardingActions.start),
            exhaustMap(action => this.service.submit(
                {
                    firstName: action.firstname,
                    name: action.name,
                    login: action.login,
                    email: action.email,
                    memberIsChild: action.isChild,
                    memberFirstName: action.memberFirstName,
                    memberName: action.memberName
                }).pipe(map(_ => onBoardingActions.success()),
                    catchError(_ => of(onBoardingActions.failure({ message: "on boarding failed" }))))
            ))
    });
}
