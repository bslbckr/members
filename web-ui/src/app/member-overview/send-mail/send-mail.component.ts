import { Component, inject } from '@angular/core';
import { SendMailService } from './send-mail.service';
import { FormControl, FormGroup, FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {map, Observable} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { AsyncPipe } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';

@Component({
  imports: [FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatButtonModule,
    MatExpansionModule,
    AsyncPipe],
  templateUrl: './send-mail.component.html',
  styles: `form {display: flex; flex-direction: column;} mat-expansion-panel {margin-bottom: 1em;}`,
  providers: [SendMailService]
})
export class SendMailComponent {

  private readonly mailSvc = inject(SendMailService);
  private readonly dialogRef = inject(MatDialogRef<SendMailComponent>);
  
  readonly form: FormGroup<{subject: FormControl<string>, body: FormControl<string>}>;

  readonly invalid$: Observable<boolean>;
  
  constructor() {
    const builder = inject(NonNullableFormBuilder);
    this.form = builder.group({
      subject: builder.control("", Validators.required),
      body: builder.control("", Validators.required)
    });

    this.invalid$ = this.form.statusChanges.pipe(takeUntilDestroyed(), map(s => s !== 'VALID'));
  }
  
  sendMail() {
    const raw = this.form.getRawValue();
    this.mailSvc.send(raw.subject, raw.body).subscribe(res => this.dialogRef.close(res));
  }

  cancel() {
    this.dialogRef.close();
  }
}
