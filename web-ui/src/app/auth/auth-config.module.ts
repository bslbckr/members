import { NgModule } from '@angular/core';
import { AuthModule } from 'angular-auth-oidc-client';
import { environment } from 'src/environments/environment';


@NgModule({
    imports: [AuthModule.forRoot({
        config: {
            configId: 'web-ui',
            authority: environment.auth.authority,
            redirectUrl: window.location.origin + '/auth/redirect',
            postLogoutRedirectUri: window.location.origin,
            clientId: environment.auth.clientId,
            scope: 'openid profile roles offline_access', // 'openid profile ' + your scopes
            responseType: 'code',
            silentRenew: true,
            //            silentRenewUrl: window.location.origin + '/auth/redirect',
            silentRenewTimeoutInSeconds: 30,
            renewTimeBeforeTokenExpiresInSeconds: 40,
            tokenRefreshInSeconds: 30,
            useRefreshToken: true,
            autoUserInfo: false,
            logLevel: environment.auth.logLevel,
            postLoginRoute: '/start',
            secureRoutes: [
                '/api'
            ]
        }
    })],
    exports: [AuthModule],
})
export class AuthConfigModule { }
