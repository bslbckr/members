import { createFeatureSelector, createSelector} from '@ngrx/store';
import { changeReducerFeatureKey, ChangesState } from './change.reducer.reducer';

const selectChangesFeature = createFeatureSelector<ChangesState>(changeReducerFeatureKey);
const selectChanges = createSelector(selectChangesFeature, (s:ChangesState) => s.changes);

export const ChangeSelectors = {
   changes: selectChanges
};
