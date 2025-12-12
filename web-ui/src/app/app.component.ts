import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Store, Action } from '@ngrx/store';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { Subject, takeUntil } from 'rxjs';
import { UserActions } from './actions/user.actions';
import { UserInfo } from './model/UserInfo';
import { decode } from 'js-base64';
import { RouterOutlet } from '@angular/router';
import { MenuComponent } from './menu/menu.component';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    imports: [RouterOutlet, MenuComponent]
})
export class AppComponent implements OnInit, OnDestroy {
    title = 'GUC Mitgliederverwaltung';
    private readonly oidcService = inject(OidcSecurityService);
    private readonly registry = inject(MatIconRegistry);
    private readonly sanitizer = inject(DomSanitizer);
    private readonly store = inject(Store);

    private readonly destroy$: Subject<void> = new Subject<void>();
  constructor() {

    this.registry.addSvgIcon('svg-revert', this.sanitizer.bypassSecurityTrustResourceUrl("assets/revert.svg"));
    this.registry.addSvgIcon('svg-save', this.sanitizer.bypassSecurityTrustResourceUrl("assets/save.svg"));
    this.registry.addSvgIcon('svg-cancel', this.sanitizer.bypassSecurityTrustResourceUrl("assets/cancel.svg"));
    this.registry.addSvgIcon('svg-bell', this.sanitizer.bypassSecurityTrustResourceUrl("assets/bell.svg"));
    this.registry.addSvgIcon('svg-profile', this.sanitizer.bypassSecurityTrustResourceUrl("assets/userProfile.svg"));
    this.registry.addSvgIcon('svg-edit', this.sanitizer.bypassSecurityTrustResourceUrl("assets/edit_document.svg"));
    this.registry.addSvgIcon('svg-factCheck', this.sanitizer.bypassSecurityTrustResourceUrl("assets/fact_check.svg"));
    this.registry.addSvgIcon('svg-send', this.sanitizer.bypassSecurityTrustResourceUrl("assets/send.svg"));
    this.registry.addSvgIcon('svg-celebration', this.sanitizer.bypassSecurityTrustResourceUrl("assets/celebration.svg"));
    }
    ngOnInit() {
        this.oidcService.checkAuth()
            .pipe(takeUntil(this.destroy$))
            .subscribe((loginResponse) => {
                let action: Action;
                if (loginResponse.isAuthenticated) {
                  const accessToken = loginResponse.accessToken.split(".")[1];

                  const decoded = decode(accessToken);
                  const parsedToken = JSON.parse(decoded) as UserInfo;
                    action = UserActions.loggedIn({ user: parsedToken });
                } else {
                    action = UserActions.loggedOut();
                }
                this.store.dispatch(action);
            });
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
        this.destroy$.unsubscribe();
    }
}

