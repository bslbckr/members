import { inject} from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Store } from '@ngrx/store';
import { Roles } from './roles';
import { UserSelectors } from './selectors/user.selectors';
import { map, Observable } from 'rxjs';

function hasRole(role: string): Observable<boolean | UrlTree> {
  const store = inject(Store);
  const router = inject(Router);
  return store.select(UserSelectors.hasRole(role))
    // if user has required role, we allow the navigation. Otherwise, we redirect
    // to /start 
    .pipe(map(r => r ? r : router.parseUrl('/start')));
}

export const hasOnBoardingRoleGuard : CanActivateFn = (_route, _state) => {
  return hasRole(Roles.onboarding);
}

export const hasBoardMemberRole : CanActivateFn = (_route, _state) => {
  return hasRole(Roles.boardMember);
}
