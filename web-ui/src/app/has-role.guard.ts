import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { UserSelectors } from './selectors/user.selectors';
import { map } from 'rxjs';

export function hasRoleGuard(requiredRole: string): CanActivateFn {
  return (_route, _state) => {
    const store = inject(Store);
    const router = inject(Router);
    return store.select(UserSelectors.hasRole(requiredRole)).pipe(map(hasRole => {
      if (hasRole) {
        return true;
      } else {
        return router.parseUrl('/start');
      }
    }));
  }
}
