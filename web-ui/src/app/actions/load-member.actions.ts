import { createActionGroup, props } from '@ngrx/store';
import { Member } from '../model/Member';

export const LoadMemberActions = createActionGroup({
    source: 'LoadMember',
    events: {
        'Load LoadMembers': props<{ memberId: string }>(),
        'Load LoadMembers Success': props<{ member: Member }>(),
        'Load LoadMembers Failure': props<{ error: unknown }>(),
        'Update Member from User': props<{ name: string, givenName: string, email: string }>()
    }
});
