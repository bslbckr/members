import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { ResourceInfo } from '../model/ResourceInfo';

export const ResourceActions = createActionGroup({
    source: 'Resource',
    events: {
        'Load Resources': emptyProps(),
        'Load Resources Success': props<{ data: ResourceInfo[] }>(),
        'Load Resources Failure': props<{ error: unknown }>(),
        'Select Resource': props<{ index: number }>()
    }
});
