import { createReducer, on } from '@ngrx/store';
import { UserInfo } from '../model/UserInfo';
import { UserActions } from '../actions/user.actions';

export interface UserState {
    user: UserInfo | null;
}

const initialState: UserState = {
    user: null
}

export const UserReducer = createReducer(initialState,
    on(UserActions.loggedIn, (state, args) => ({ ...state, user: args.user })),
    on(UserActions.loggedOut, (state, _) => ({ ...state, user: null })));
