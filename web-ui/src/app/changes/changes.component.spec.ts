import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangesComponent } from './changes.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ChangeService } from './change.service';

describe('ChangesComponent', () => {
  let component: ChangesComponent;
  let fixture: ComponentFixture<ChangesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [ChangeService, provideHttpClient(), provideHttpClientTesting()],
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
