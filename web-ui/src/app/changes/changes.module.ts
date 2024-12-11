import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { ChangesEffects } from './state/changes.effects';
import { CommonModule } from '@angular/common';
import { ChangeService } from './change.service';
import { ChangesRoutingModule } from './changes-routing.module';
import { ChangesComponent } from './changes.component';
import { StoreModule } from '@ngrx/store';
import * as fromChange from './state/change.reducer.reducer';
import { MatCardModule } from '@angular/material/card';


@NgModule({
  declarations: [
    ChangesComponent
  ],
  providers: [ChangeService],
  imports: [
    CommonModule,
    ChangesRoutingModule,
    StoreModule.forFeature(fromChange.changeReducerFeatureKey, fromChange.reducer),
    EffectsModule.forFeature([ChangesEffects]),
    MatCardModule
  ]
})
export class ChangesModule { }
