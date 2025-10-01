import { createFeatureSelector, createSelector } from '@ngrx/store';
import { MemberState, memberFeatureKey } from '../reducers/member.reducer';
import { ResourceSelectors } from './resources.selectors';
import { UserSelectors } from './user.selectors';

export const selectMemberFeature = createFeatureSelector<MemberState>(memberFeatureKey);
const selectMemberData = createSelector(selectMemberFeature, state => state.modifiedMember);
const selectName = createSelector(selectMemberData, mem => mem?.name);
const selectGivenName = createSelector(selectMemberData, mem => mem?.givenName);
const selectDayOfBirth = createSelector(selectMemberData, mem => mem?.dayOfBirth);
const selectQualifiesForYouthMembership = createSelector(selectDayOfBirth, dob => {
  if (dob === undefined) {
    return false;
  }
  const dayOfBirth = new Date(dob);
  const yearOfBirth = dayOfBirth.getFullYear();
  const year = new Date().getFullYear();
  return year - yearOfBirth <= 18;
});
const selectEntryDate = createSelector(selectMemberData, mem => mem?.entryDate);
const selectLoadedMember = createSelector(selectMemberFeature, state => state.loadedMember);
const selectLoadedStatus = createSelector(selectLoadedMember, m => m?.state);
const selectIsUpdateNecessary = createSelector(ResourceSelectors.updateAllowed,
    UserSelectors.currentUser,
    selectMemberData,
    (allowed, user, member) => {
        if (allowed && user && member) {
            if (user.family_name === member.name) {
                if (user.given_name === member.givenName) {
                    if (user.email === member.email) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    });
const selectMembershipIsCancelled = createSelector(selectMemberData, m => m?.exitDate !== null);
const selectStoreSuccess = createSelector(selectMemberFeature, state => state.storeSuccessful);
const selectStoreSucceeded = createSelector(selectStoreSuccess, state => state ?? false);
const selectStoreFailed = createSelector(selectStoreSuccess, val => val === undefined ? false : val == false);
const selectUpdated = createSelector(selectMemberFeature, state => state.masterDataUpdated);
export const MemberSelectors = {
    selectName: selectName,
    selectGivenName: selectGivenName,
    selectDayOfBirth: selectDayOfBirth,
    selectEntryDate: selectEntryDate,
    selectMember: selectMemberData,
    selectLoadedStatus: selectLoadedStatus,
    updateNecessary: selectIsUpdateNecessary,
    dataUpdated: selectUpdated,
    membershipCancelled: selectMembershipIsCancelled,
    qualifiesForYouthMembership: selectQualifiesForYouthMembership
}

export const StoreSelectors = {
    success: selectStoreSucceeded,
    failure: selectStoreFailed
}
