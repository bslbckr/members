import { createActionGroup, emptyProps, props } from '@ngrx/store';

export const onBoardingActions = createActionGroup({
    source: 'OnBoarding',
    events: {
        'start': props<{ firstname: string, name: string, email: string, login: string, isChild: boolean, memberFirstName: string, memberName: string }>(),
        'success': emptyProps(),
        'failure': props<{ message: string }>()
    }
});
