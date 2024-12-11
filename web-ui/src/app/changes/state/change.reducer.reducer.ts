import { createFeature, createReducer, on } from '@ngrx/store';
import { ChangesActions } from './changes.action.actions';
import { StateChanges } from '../state.changes';

export const changeReducerFeatureKey = 'changeReducer';

export interface ChangesState {
  changes: StateChanges[]
}

export const initialState: ChangesState = {
  changes: []
};

export const reducer = createReducer(
  initialState,
  
  on(ChangesActions.loadChangesSuccess, (state, act) => ({
    ...state,
    changes: act.changes
  })),

);

export const changeReducerFeature = createFeature({
  name: changeReducerFeatureKey,
  reducer,
});

