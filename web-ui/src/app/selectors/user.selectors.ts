import { createSelector, createFeatureSelector } from '@ngrx/store';
import { UserState } from '../reducers/user.reducer';

const selectUserFeature = createFeatureSelector<UserState>("user");
const selectCurrentUser = createSelector(selectUserFeature, s => s.user);
const selectLoggedIn = createSelector(selectCurrentUser, u => u !== null);
const selectRoles = createSelector(selectCurrentUser, u => u !== null
    && u.resource_access?.members?.roles !== null ? u.resource_access?.members?.roles : []);
const selectHasRole = (role: string) => createSelector(selectRoles, r => r.findIndex(v => v === role) !== -1);

export const UserSelectors = {
    loggedIn: selectLoggedIn,
    hasRole: selectHasRole,
    currentUser: selectCurrentUser
}
