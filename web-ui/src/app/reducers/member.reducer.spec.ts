import { MemberReducer, initialState } from './member.reducer';

describe('Member Reducer', () => {
    describe('an unknown action', () => {
        it('should return the previous state', () => {
            const action = {} as any;

            const result = MemberReducer(initialState, action);

            expect(result).toBe(initialState);
        });
    });
});
