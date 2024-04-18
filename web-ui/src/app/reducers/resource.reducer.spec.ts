import { ResourceReducer, initialState } from './resource.reducer';

describe('Resource Reducer', () => {
    describe('an unknown action', () => {
        it('should return the previous state', () => {
            const action = {} as any;

            const result = ResourceReducer(initialState, action);

            expect(result).toBe(initialState);
        });
    });
});
