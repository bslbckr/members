import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { KeycloakInitOptions, KeycloakLoginOptions } from 'keycloak-js';
import { Observable, defer } from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export class AuthenticationService {

    private readonly keycloak: Keycloak;

    constructor() {
        const kcOpts = { url: "https://login.goldfingers-potsdam.de", realm: "guc", clientId: "members" };
        this.keycloak = new Keycloak(kcOpts);
        const initOpts: KeycloakInitOptions = {};
        this.keycloak.init(initOpts);
    }

    ensureAuthentication(): Observable<boolean> {
        return defer(async () => {
            if (this.keycloak.authenticated) {
                return true;
            }
            const loginOpts: KeycloakLoginOptions = {
                redirectUri: "http://localhost:4200/auth/redirect",
                scope: 'openid'
            }

            await this.keycloak.login(loginOpts);
            return this.keycloak.authenticated as boolean;
        });
    }
}
