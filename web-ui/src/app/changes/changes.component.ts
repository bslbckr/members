import { Component, inject, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { ChangesActions } from './state/changes.action.actions';
import { ChangeSelectors } from './state/changes.selector';
import { MatCard, MatCardHeader, MatCardContent } from '@angular/material/card';
import { NgFor, AsyncPipe, DatePipe } from '@angular/common';


@Component({
    selector: 'app-changes',
    templateUrl: './changes.component.html',
    styleUrl: './changes.component.css',
  imports: [MatCard, MatCardHeader, MatCardContent, NgFor, AsyncPipe, DatePipe],
  standalone: true
})
export class ChangesComponent implements OnInit {

  private readonly store = inject(Store);

  readonly changes$ = this.store.select(ChangeSelectors.changes);

  ngOnInit() {
    this.store.dispatch(ChangesActions.loadChanges());
  }

}
