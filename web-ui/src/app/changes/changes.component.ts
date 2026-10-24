import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { MatCard, MatCardHeader, MatCardContent } from '@angular/material/card';
import { DatePipe } from '@angular/common';
import { ChangeService } from './change.service';


@Component({
    selector: 'app-changes',
    templateUrl: './changes.component.html',
    styleUrl: './changes.component.css',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCard, MatCardHeader, MatCardContent, DatePipe]
})
export class ChangesComponent {
  private readonly changeSvc = inject(ChangeService);
  
  readonly changes$ = this.changeSvc.getChanges();
  readonly entries = this.changeSvc.getEntries();
  readonly cancellations = this.changeSvc.getCancellations();
}
