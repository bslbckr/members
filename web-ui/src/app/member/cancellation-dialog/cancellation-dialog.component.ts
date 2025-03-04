import { AfterViewInit, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { distinctUntilChanged, map } from 'rxjs';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatSelect } from '@angular/material/select';
import { NgFor, AsyncPipe } from '@angular/common';
import { MatOption } from '@angular/material/core';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-cancel-dialog',
    templateUrl: './cancellation-dialog.component.html',
    styleUrls: ['./cancellation-dialog.component.css'],
  imports: [MatDialogTitle, CdkScrollable, MatDialogContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, MatSelect, NgFor, MatOption, MatSlideToggle, MatDialogActions, MatButton, AsyncPipe],
  standalone: true
})
export class CancellationDialogComponent implements AfterViewInit {
    private readonly builder = inject(NonNullableFormBuilder);
    readonly dialogData: CancellationDialogData = inject(MAT_DIALOG_DATA);
    private readonly dialogRef: MatDialogRef<CancellationDialogComponent, Date> = inject(MatDialogRef<CancellationDialogComponent, Date>);

    readonly cancellationGroup = this.builder.group({
        selectedIndex: [-1, [Validators.required, Validators.min(0)]],
        cancellationConfirmed: this.builder.control({ value: false, disabled: true }, { validators: Validators.requiredTrue })
    });

    readonly groupInvalid$ = this.cancellationGroup.statusChanges.pipe(takeUntilDestroyed(),
        map(s => s !== "VALID"));
    readonly _x = this.cancellationGroup.valueChanges.pipe(takeUntilDestroyed(),
        map(v => v.selectedIndex),
        map(i => i !== -1),
        distinctUntilChanged())
        .subscribe(dateSel => {
            if (dateSel) {
                this.cancellationGroup.get("cancellationConfirmed")?.enable({ emitEvent: true });
                this.cancellationGroup.updateValueAndValidity({ emitEvent: true });
            }
        });

    cancel() {
        this.dialogRef.close();
    }

    ok() {
        const selectedIndex = this.cancellationGroup.value.selectedIndex ?? -1;
        if (selectedIndex === -1) {
            this.cancel();
        } else {
            const cancellationDate = this.dialogData.allowedCancellationDate[selectedIndex];
            this.dialogRef.close(cancellationDate);
        }
    }
    ngAfterViewInit() {
        //        this.cancellationGroup.setValue({ selectedIndex: -1, cancellationConfirmed: false });
        this.cancellationGroup.updateValueAndValidity({ emitEvent: true });
        this.cancellationGroup.reset();
    }
}

export interface CancellationDialogData {
    allowedCancellationDate: Date[],
    selectedDate: Date | null
}
