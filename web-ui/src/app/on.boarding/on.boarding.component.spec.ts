import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OnBoardingComponent } from './on.boarding.component';

describe('OnBoardingComponent', () => {
  let component: OnBoardingComponent;
  let fixture: ComponentFixture<OnBoardingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [OnBoardingComponent]
});
    fixture = TestBed.createComponent(OnBoardingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
