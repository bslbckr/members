import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Store } from '@ngrx/store';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { Subject, takeUntil } from 'rxjs';
import { UserActions } from './actions/user.actions';
import { Action } from '@ngrx/store/src/models';
import { UserInfo } from './model/UserInfo';
const REVERT_ICON =
    `
<svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
<path d="M0 0h24v24H0z" fill="none"/>
<path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
</svg>
`;

const SAVE_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
<path d="M0 0h24v24H0z" fill="none"/>
<path d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/></svg>`;

const CANCEL_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" enable-background="new 0 0 24 24" height="24px" viewBox="0 0 24 24" width="24px" ><g><rect fill="none" height="24" width="24"/></g><g><g><path d="M15.18,10.94c0.2-0.44,0.32-0.92,0.32-1.44C15.5,7.57,13.93,6,12,6c-0.52,0-1,0.12-1.44,0.32L15.18,10.94z"/><path d="M12,2C6.48,2,2,6.48,2,12s4.48,10,10,10s10-4.48,10-10S17.52,2,12,2z M4,12c0-1.85,0.63-3.55,1.69-4.9l2.86,2.86 c0.21,1.56,1.43,2.79,2.99,2.99l2.2,2.2C13.17,15.05,12.59,15,12,15c-2.32,0-4.45,0.8-6.14,2.12C4.7,15.73,4,13.95,4,12z M12,20 c-1.74,0-3.34-0.56-4.65-1.5C8.66,17.56,10.26,17,12,17s3.34,0.56,4.65,1.5C15.34,19.44,13.74,20,12,20z M18.31,16.9L7.1,5.69 C8.45,4.63,10.15,4,12,4c4.42,0,8,3.58,8,8C20,13.85,19.37,15.54,18.31,16.9z"/></g></g></svg>`;

const BELL_ICON = `<svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24"><path d="M0 0h24v24H0V0z" fill="none"/><path d="M18 16v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-5 0h-2v-2h2v2zm0-4h-2V8h2v4zm-1 10c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2z"/></svg>`;

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
    title = 'GUC Mitgliederverwaltung';
    private readonly oidcService = inject(OidcSecurityService);
    private readonly registry = inject(MatIconRegistry);
    private readonly sanitizer = inject(DomSanitizer);
    private readonly store = inject(Store);

    private readonly destroy$: Subject<void> = new Subject<void>();
    constructor() {
        this.registry.addSvgIconLiteral('svg-revert', this.sanitizer.bypassSecurityTrustHtml(REVERT_ICON));
        this.registry.addSvgIconLiteral('svg-save', this.sanitizer.bypassSecurityTrustHtml(SAVE_ICON));
        this.registry.addSvgIconLiteral('svg-cancel', this.sanitizer.bypassSecurityTrustHtml(CANCEL_ICON));
        this.registry.addSvgIconLiteral('svg-bell', this.sanitizer.bypassSecurityTrustHtml(BELL_ICON));
    }
    ngOnInit() {
        this.oidcService.checkAuth()
            .pipe(takeUntil(this.destroy$))
            .subscribe((loginResponse) => {
                let action: Action;
                if (loginResponse.isAuthenticated) {
                    const accessToken = loginResponse.accessToken.split(".")[1];
                    const parsedToken = JSON.parse(window.atob(accessToken)) as UserInfo;
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

