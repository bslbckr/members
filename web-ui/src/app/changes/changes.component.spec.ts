import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangesComponent } from './changes.component';
import { provideMockStore } from '@ngrx/store/testing';

describe('ChangesComponent', () => {
  let component: ChangesComponent;
  let fixture: ComponentFixture<ChangesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideMockStore()],
      imports: [ChangesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChangesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
