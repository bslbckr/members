import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-cancel-dialog',
    templateUrl: './cancellation-dialog.component.html',
    styleUrls: ['./cancellation-dialog.component.css']
})
export class CancellationDialogComponent {

    readonly dialogData: CancellationDialogData = inject(MAT_DIALOG_DATA);
    private readonly dialogRef: MatDialogRef<CancellationDialogComponent, Date> = inject(MatDialogRef<CancellationDialogComponent, Date>);

    selectedIndex: number = -1;

    cancel() {
        this.dialogRef.close();
    }

    ok() {
        this.dialogRef.close(this.dialogData.allowedCancellationDate[this.selectedIndex]);
    }
}

export interface CancellationDialogData {
    allowedCancellationDate: Date[],
    selectedDate: Date | null
}
