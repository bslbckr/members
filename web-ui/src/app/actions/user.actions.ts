import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { UserInfo } from '../model/UserInfo';

export const UserActions = createActionGroup({
    source: "User",
    events: {
        "logged in": props<{ user: UserInfo }>(),
        "logged out": emptyProps()
    }
});
