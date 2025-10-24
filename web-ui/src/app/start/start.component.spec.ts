import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartComponent } from './start.component';
import { provideMockStore } from '@ngrx/store/testing';

describe('StartComponent', () => {
  let component: StartComponent;
  let fixture: ComponentFixture<StartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideMockStore()],
      imports: [StartComponent]
    });
    fixture = TestBed.createComponent(StartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
