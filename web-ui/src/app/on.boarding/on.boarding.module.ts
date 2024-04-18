import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OnBoardingRoutingModule } from './on.boarding-routing.module';
import { OnBoardingComponent } from './on.boarding.component';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { OnboardingEffects } from './+state/onboarding.effects';
import { MatFormFieldModule } from '@angular/material/form-field';
import { OnBoardingService } from './on-boarding.service';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@NgModule({
    declarations: [
        OnBoardingComponent
    ],
    imports: [
        CommonModule,
        OnBoardingRoutingModule,
        MatButtonModule,
        MatCardModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatSlideToggleModule,
        MatTooltipModule,
        FormsModule,
        ReactiveFormsModule,
        EffectsModule.forFeature(OnboardingEffects),
    ],
    providers: [
        OnBoardingService
    ]
})
export class OnBoardingModule { }
