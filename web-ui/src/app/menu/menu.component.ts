import { Component, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { UserSelectors } from '../selectors/user.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-menu',
    templateUrl: './menu.component.html',
    styleUrls: ['./menu.component.css']
})
export class MenuComponent {
    private readonly store = inject(Store);
    readonly hasOnboardingRole$ = this.store.select(UserSelectors.hasRole("onboarding"))
        .pipe(takeUntilDestroyed());
}
