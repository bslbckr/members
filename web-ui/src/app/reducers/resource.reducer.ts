import { createReducer, on } from '@ngrx/store';
import { ResourceActions } from '../actions/resource.actions';
import { ResourceInfo } from '../model/ResourceInfo';


export const resourceFeatureKey = 'resource';

export interface State {
    users: ResourceInfo[],
    selectedIndex: number
}

export const initialState: State = {
    users: [],
    selectedIndex: -1
};

export const ResourceReducer = createReducer(
    initialState,
    on(ResourceActions.loadResourcesSuccess,
        (state, args) => ({
            ...state,
            users: args.data
        })),
    on(ResourceActions.selectResource, (state, args) => ({
        ...state,
        selectedIndex: args.index
    }))
);
