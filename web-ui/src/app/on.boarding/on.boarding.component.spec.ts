import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OnBoardingComponent } from './on.boarding.component';
import { MockStore, provideMockStore,  } from '@ngrx/store/testing';
import { onBoardingActions } from '../actions/actions';
import { MATERIAL_ANIMATIONS } from '@angular/material/core';

describe('OnBoardingComponent', () => {
  let component: OnBoardingComponent;
  let fixture: ComponentFixture<OnBoardingComponent>;
  let mockStore: MockStore;
  let spy: jasmine.Spy;

  
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideMockStore({
        initialState: {
          "onBoarding": {
            onBoardingSuccessful: null,
            errorMessage: null
          }
        }
      }),
        {provide: MATERIAL_ANIMATIONS, useValue: {animationsDisabled:true}}],
      imports: [OnBoardingComponent] 
    });
    fixture = TestBed.createComponent(OnBoardingComponent);
    mockStore = TestBed.inject(MockStore);
    spy = spyOn(mockStore, 'dispatch');
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch an onboarding message', () => {
    component.form.patchValue({
      firstName: "Foo",
      name: "Test bar",
      email: "foo@test.bar",
      login: "foo"
    });
    component.send();
    expect(spy).toHaveBeenCalledOnceWith(onBoardingActions.start({
        firstname: "Foo",
        name: "Test bar",
        email: "foo@test.bar",
        login: "foo",
        isChild: false,
        memberFirstName: "",
        memberName: ""
    }));
  });

});
