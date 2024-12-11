import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { StateChanges } from '../state.changes';

export const ChangesActions = createActionGroup({
  source: 'Changes',
  events: {
    'Load Changes': emptyProps(),
    'Load Changes Success': props<{ changes: StateChanges[] }>(),
    'Load Changes Failure': props<{ error: Error }>(),
  }
});
