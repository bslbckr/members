import { Component, OnInit, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { ResourceActions } from '../actions/resource.actions';
import { ResourceSelectors } from '../selectors/resources.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-start',
    templateUrl: './start.component.html',
    styleUrls: ['./start.component.css'],
    standalone: false
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
