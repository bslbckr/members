import { AfterViewInit, Component, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Observable, distinctUntilChanged, map } from 'rxjs';

@Component({
    selector: 'app-cancel-dialog',
    templateUrl: './cancellation-dialog.component.html',
    styleUrls: ['./cancellation-dialog.component.css'],
    standalone: false
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
