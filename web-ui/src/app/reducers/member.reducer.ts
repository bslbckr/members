import { createReducer, on } from '@ngrx/store';
import { LoadMemberActions } from '../actions/load-member.actions';
import { Member } from '../model/Member';
import { ModifyMemberActions } from '../actions/modify-member.actions';

export const memberFeatureKey = 'member';

export interface MemberState {
    loadedMember?: Readonly<Member>,
    modifiedMember?: Member,
    masterDataUpdated: boolean,
    storeSuccessful?: boolean,
    storeErrorMessage?: string
}

export const initialState: MemberState = {
    masterDataUpdated: false
};

export const MemberReducer = createReducer(
    initialState,
    on(LoadMemberActions.loadLoadMembersSuccess, (state, action) => ({
        ...state,
        loadedMember: action.member,
        modifiedMember: action.member,
        storeErrorMessage: undefined,
        storeSuccessful: undefined,
        masterDataUpdated: false
    })),
    on(LoadMemberActions.loadLoadMembersFailure, (state, _) => state),
    on(ModifyMemberActions.revert, (state, _) => ({
        ...state, modifiedMember: {
            name: state.loadedMember?.name || '',
            givenName: state.loadedMember?.givenName || '',
            city: state.loadedMember?.city || '',
            dayOfBirth: state.loadedMember?.dayOfBirth || new Date(),
            dfvDiscount: state.loadedMember?.dfvDiscount || false,
            dfvNumber: state.loadedMember?.dfvNumber || 0,
            dse: state.loadedMember?.dse || false,
            email: state.loadedMember?.email || '',
            emailList: state.loadedMember?.emailList || false,
            entryDate: state.loadedMember?.entryDate || new Date(),
            exitDate: state.loadedMember?.exitDate || new Date(),
            gender: state.loadedMember?.gender || 'female',
            id: state.loadedMember?.id || '',
            mobile: state.loadedMember?.mobile || '',
            state: state.loadedMember?.state || 'passiv',
            stateEffective: state.loadedMember?.stateEffective || new Date(),
            street: state.loadedMember?.street || '',
            zipCode: state.loadedMember?.zipCode || ''
        }
    })),
    on(ModifyMemberActions.storeFailed, (state, action) => ({
        ...state,
        storeSuccessful: false,
        storeErrorMessage: action.error.message
    })),
    on(ModifyMemberActions.storeSuccess, (state, _) => ({
        ...state,
        storeSuccessful: true
    })),
    on(LoadMemberActions.updateMemberFromUser, (state, action) => {
        const res: MemberState = { ...state };

        if (state.modifiedMember) {
            res.modifiedMember = {
                ...state.modifiedMember,
                name: action.name,
                givenName: action.givenName,
                email: action.email
            };
            res.masterDataUpdated = true;
        }
        return res;

    }),
  on(ModifyMemberActions.navigateToStart, (state,action) => ({
    ...state,
    storeSuccessful: undefined,
    storeErrorMessage: undefined
  }))
);
