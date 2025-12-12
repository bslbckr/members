import { Component, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { UserSelectors } from '../selectors/user.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { environment } from 'src/environments/environment';
import { DomSanitizer } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { MatDivider } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AsyncPipe } from '@angular/common';

@Component({
    selector: 'app-menu',
    templateUrl: './menu.component.html',
    styleUrls: ['./menu.component.css'],
    imports: [RouterModule, MatDivider, MatIconModule, MatToolbarModule, AsyncPipe]
})
export class MenuComponent {
    private readonly store = inject(Store);
    private readonly sanitizer = inject(DomSanitizer);
    readonly hasOnboardingRole$ = this.store.select(UserSelectors.hasRole("onboarding"))
      .pipe(takeUntilDestroyed());
  readonly hasBoardRole$ = this.store.select(UserSelectors.hasRole("board-member"))
    .pipe(takeUntilDestroyed());
    readonly accountUrl = this.sanitizer.bypassSecurityTrustUrl(environment.auth.authority + "/account");
}
