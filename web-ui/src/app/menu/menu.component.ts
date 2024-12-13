import { Component, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { UserSelectors } from '../selectors/user.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { environment } from 'src/environments/environment';
import { DomSanitizer } from '@angular/platform-browser';
import { Roles } from '../roles';

@Component({
    selector: 'app-menu',
    templateUrl: './menu.component.html',
    styleUrls: ['./menu.component.css']
})
export class MenuComponent {
    private readonly store = inject(Store);
    private readonly sanitizer = inject(DomSanitizer);
    readonly hasOnboardingRole$ = this.store.select(UserSelectors.hasRole(Roles.onboarding))
      .pipe(takeUntilDestroyed());
  readonly hasBoardRole$ = this.store.select(UserSelectors.hasRole(Roles.boardMember))
    .pipe(takeUntilDestroyed());
    readonly accountUrl = this.sanitizer.bypassSecurityTrustUrl(environment.auth.authority + "/account");
}
