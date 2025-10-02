import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Member } from '../model/Member';

export const ModifyMemberActions = createActionGroup({
    source: 'Modify Member',
    events: {
      'Revert': emptyProps(),
      'Store': props<{ member: Member }>(),
      'Store success': emptyProps(),
      'Store failed': props<{ error: Error }>(),
      'Navigate to start': emptyProps()
    }
});
