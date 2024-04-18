import { createFeature, createReducer, on } from '@ngrx/store';

import { onBoardingActions } from '../actions/actions';


export const onBoardingFeatureKey = 'onBoarding';

export interface OnBoardingState {
    onBoardingSuccessful: boolean | null;
    errorMessage: string | null;
}

export const initialState: OnBoardingState = {
    onBoardingSuccessful: null,
    errorMessage: null
};

export const OnBoardingReducer = createReducer(
    initialState,
    on(onBoardingActions.failure, (state, args) => ({
        ...state,
        onBoardingSuccessful: false,
        errorMessage: args.message
    })),
    on(onBoardingActions.success, state => ({
        ...state,
        onBoardingSuccessful: true,
        errorMessage: null
    }))

);


export const onBoardingFeature = createFeature({
    name: onBoardingFeatureKey,
    reducer: OnBoardingReducer,
});

