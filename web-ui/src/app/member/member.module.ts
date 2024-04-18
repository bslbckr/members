import { NgModule } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MemberRoutingModule } from './member-routing.module';
import { MemberComponent } from './member.component';
import { EffectsModule } from '@ngrx/effects';
import { MemberEffects } from './effects/member.effects';
import { MemberService } from './member.service';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { CancellationDialogComponent } from './cancellation-dialog/cancellation-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';


@NgModule({
    declarations: [
        MemberComponent,
        CancellationDialogComponent
    ],
    imports: [
        CommonModule,
        AsyncPipe,
        FormsModule,
        MatButtonModule,
        MatCardModule,
        MatDatepickerModule,
        MatDialogModule,
        MatExpansionModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatStepperModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatNativeDateModule,
        MatSnackBarModule,
        MatTooltipModule,
        MemberRoutingModule,
        ReactiveFormsModule,
        EffectsModule.forFeature([MemberEffects]),
    ],
    providers: [MemberService]
})
export class MemberModule { }
