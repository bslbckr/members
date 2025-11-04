import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CancellationDialogComponent } from './cancellation-dialog.component';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTestDialogOpener, MatTestDialogOpenerModule } from '@angular/material/dialog/testing';

describe('CancellationDialogComponent', () => {
  let fixture: ComponentFixture<MatTestDialogOpener>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({

      imports: [CancellationDialogComponent, MatTestDialogOpenerModule]
    }).compileComponents();
    fixture = TestBed.createComponent(MatTestDialogOpener.withComponent(CancellationDialogComponent, {data:{ allowedCancellationDate: [new Date()], selectedDate: null}}));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
