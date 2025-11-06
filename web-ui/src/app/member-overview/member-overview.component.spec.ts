import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MemberOverviewComponent } from './member-overview.component';
import { MemberOverview, MemberOverviewService } from './member-overview.service';
import {Observable, of} from 'rxjs';
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import {MatButtonHarness } from '@angular/material/button/testing';
import { MatDialogHarness } from '@angular/material/dialog/testing';
import { MATERIAL_ANIMATIONS } from '@angular/material/core';
describe('MemberOverviewComponent', () => {
  let component: MemberOverviewComponent;
  let fixture: ComponentFixture<MemberOverviewComponent>;
  let loader: HarnessLoader;
  beforeEach(async () => {

    await TestBed.configureTestingModule({
      providers: [{provide: MATERIAL_ANIMATIONS, useValue: {animationsDisabled: true}}],
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

    loader = TestbedHarnessEnvironment.documentRootLoader(fixture);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it ('should find button', async () =>{

    const button = await loader.getHarness(MatButtonHarness.with({selector: "#send-mail-button"}));
    expect(button).toBeTruthy();
    await button.click();
    await fixture.whenRenderingDone();
    
    const dialog = await loader.getAllHarnesses(MatDialogHarness);
    expect(dialog).toHaveSize(1);
  });
});
