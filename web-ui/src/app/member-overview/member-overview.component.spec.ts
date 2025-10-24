import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MemberOverviewComponent } from './member-overview.component';
import { MemberOverview, MemberOverviewService } from './member-overview.service';
import {Observable, of} from 'rxjs';

describe('MemberOverviewComponent', () => {
  let component: MemberOverviewComponent;
  let fixture: ComponentFixture<MemberOverviewComponent>;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [MemberOverviewComponent]
    }).overrideProvider(MemberOverviewService,
      {useValue: {
        loadMemberOverview():Observable<MemberOverview[]>{
          return of([]);
        }
      }
      })
      .compileComponents();

    fixture = TestBed.createComponent(MemberOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
