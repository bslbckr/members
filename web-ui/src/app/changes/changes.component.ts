import { Component, inject } from '@angular/core';
import { MatCard, MatCardHeader, MatCardContent } from '@angular/material/card';
import { NgFor, AsyncPipe, DatePipe } from '@angular/common';
import { ChangeService } from './change.service';


@Component({
    selector: 'app-changes',
    templateUrl: './changes.component.html',
    styleUrl: './changes.component.css',
    imports: [MatCard, MatCardHeader, MatCardContent, NgFor, AsyncPipe, DatePipe]
})
export class ChangesComponent {

  private readonly changeSvc = inject(ChangeService);
  readonly changes$ = this.changeSvc.getChanges();
  readonly entries = this.changeSvc.getEntries();
  readonly cancellations = this.changeSvc.getCancellations();

}
