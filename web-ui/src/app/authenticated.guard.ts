import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { map } from 'rxjs';

export const authenticatedGuard: CanActivateFn = (route, state) => {
    const service: OidcSecurityService = inject(OidcSecurityService);
    return service.isAuthenticated$.pipe(map(authRes => {
        if (authRes.isAuthenticated) {
            return true;
        }
        service.authorize();
        return false;
    }));
};
