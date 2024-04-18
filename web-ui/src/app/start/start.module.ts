import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { StartRoutingModule } from './start-routing.module';
import { StartComponent } from './start.component';
import { EffectsModule } from '@ngrx/effects';
import { StartEffects } from './effects/start.effects';
import { ResourceService } from './resource.service';


@NgModule({
    declarations: [
        StartComponent
    ],
    imports: [
        CommonModule,
        MatButtonModule,
        MatCardModule,
        StartRoutingModule,
        EffectsModule.forFeature([StartEffects]),
        //        StoreModule.forFeature(fromResource.resourceFeatureKey, fromResource.reducer)
    ],
    providers: [
        ResourceService
    ]
})
export class StartModule { }
