import { createFeatureSelector, createSelector } from '@ngrx/store';
import { State, resourceFeatureKey } from '../reducers/resource.reducer';

export const RESOURCE_FEATURE: string = 'resources';

const selectResourceFeature = createFeatureSelector<State>(resourceFeatureKey);

const selectUsers = createSelector(selectResourceFeature, state => state.users);
const selectIndex = createSelector(selectResourceFeature, state => state.selectedIndex);

const selectResource = createSelector(selectUsers, selectIndex, (users, index) => {
    if (users && index >= 0 && index < users.length) {
        // accessing the users array should be safe.
        return users[index];
    }
    return null;
})
const selectMemberId = createSelector(selectResource, resource => resource?.memberId);
const selectRole = createSelector(selectResource, resource => resource?.owner.toLowerCase());
const selectUpdateAllowed = createSelector(selectRole, role => role === "self" || role === "owner");

export const ResourceSelectors = {
    selectUsers: selectUsers,
    selectMemberId: selectMemberId,
    selectRole: selectRole,
    updateAllowed: selectUpdateAllowed
}
