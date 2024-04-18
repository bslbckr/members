import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CancellationDialogComponent } from './cancellation-dialog.component';

describe('CancellationDialogComponent', () => {
  let component: CancellationDialogComponent;
  let fixture: ComponentFixture<CancellationDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CancellationDialogComponent]
    });
    fixture = TestBed.createComponent(CancellationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
