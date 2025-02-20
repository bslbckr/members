import { Component, OnInit, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { ResourceActions } from '../actions/resource.actions';
import { ResourceSelectors } from '../selectors/resources.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCard, MatCardHeader, MatCardTitle, MatCardContent, MatCardFooter } from '@angular/material/card';
import { NgFor, AsyncPipe } from '@angular/common';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-start',
    templateUrl: './start.component.html',
    styleUrls: ['./start.component.css'],
    imports: [MatCard, MatCardHeader, MatCardTitle, MatCardContent, NgFor, MatButton, MatCardFooter, AsyncPipe]
})
export class StartComponent implements OnInit {

    private readonly store = inject(Store);

    readonly users$ = this.store.select(ResourceSelectors.selectUsers)
        .pipe(takeUntilDestroyed());

    ngOnInit(): void {
        this.store.dispatch(ResourceActions.loadResources());
    }

    selectIndex(idx: number): void {
        this.store.dispatch(ResourceActions.selectResource({ index: idx }));
    }
}
