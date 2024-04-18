import { createFeatureSelector, createSelector } from '@ngrx/store';
import { OnBoardingState, onBoardingFeatureKey } from '../reducers/reducers';

const feature = createFeatureSelector<OnBoardingState>(onBoardingFeatureKey);

const selectOnBoardingStatus = createSelector(feature, s => s.onBoardingSuccessful);
const selectOnBoardindSucceeded = createSelector(selectOnBoardingStatus, stat => stat === true);
const selectOnBoardingFailed = createSelector(selectOnBoardingStatus, stat => stat === false);
const selectErrorMessage = createSelector(feature, s => s.errorMessage);

export const onboardingSelectors = {
    success: selectOnBoardindSucceeded,
    failure: selectOnBoardingFailed,
    message: selectErrorMessage
};
