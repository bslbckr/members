import { onBoardingActions } from '../actions/actions';
import {initialState, OnBoardingReducer} from './reducers';

describe('OnBoarding Reducer', () => {

  it('should set success', () => {
    const state = OnBoardingReducer(initialState, onBoardingActions.success());
    expect(state.onBoardingSuccessful).toBeTrue();
  });

  it('should reset success state', () => {
    const state = OnBoardingReducer({onBoardingSuccessful: false, errorMessage: "foo"},
      onBoardingActions.start({firstname: "foo", name: "bar", email: "test", isChild: false, memberFirstName: "", memberName: "", login: "login"}));
    expect(state.onBoardingSuccessful).toBeNull();
  });

  it('should set failure', () => {
    const state = OnBoardingReducer(initialState, onBoardingActions.failure({message: "test failed"}));
    expect(state.onBoardingSuccessful).toBeFalse();
    expect(state.errorMessage).toBe("test failed");
  });
})
