import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { provideStore, provideState } from '@ngrx/store';
import { UserReducer } from './reducers/user.reducer';
import { ResourceReducer } from './reducers/resource.reducer';
import { MemberReducer } from './reducers/member.reducer';
import { OnBoardingReducer } from './reducers/reducers';
import { provideEffects } from '@ngrx/effects';
import { provideRouter, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { authenticatedGuard } from './authenticated.guard';
import { AppComponent } from './app.component';
import { authInterceptor, OidcSecurityService, PassedInitialConfig, provideAuth } from 'angular-auth-oidc-client';
import { environment } from 'src/environments/environment';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { StartEffects } from './start/effects/start.effects';
import { ResourceService } from './start/resource.service';
import { MemberEffects } from './member/effects/member.effects';
import { MemberService } from './member/member.service';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import { OnboardingEffects } from './on.boarding/+state/onboarding.effects';
import { OnBoardingService } from './on.boarding/on-boarding.service';
import { ChangesEffects } from './changes/state/changes.effects';
import { ChangeService } from './changes/change.service';
import * as changes from './changes/state/change.reducer.reducer';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  {
    path: 'start',
    loadComponent: () => import('./start/start.component').then(m => m.StartComponent),
    canActivate: [authenticatedGuard],
    providers: [
      provideState({ name:"resource", reducer: ResourceReducer}),
      provideEffects(StartEffects),
      ResourceService]
  },
  {
    path: 'member',
    loadComponent: () => import('./member/member.component').then(m => m.MemberComponent),
    canActivate: [authenticatedGuard],
    providers: [
      provideState({name: "member", reducer: MemberReducer}),
      provideEffects(MemberEffects),
      provideNativeDateAdapter(),
      MemberService]
  },
  {
    path: 'onboarding',
    loadComponent: () => import('./on.boarding/on.boarding.component').then(m => m.OnBoardingComponent),
    providers: [
      provideState({name:"onBoarding", reducer: OnBoardingReducer}),
      provideEffects(OnboardingEffects), OnBoardingService]
  },
  {
    path: 'changes',
    loadComponent: () => import('./changes/changes.component').then(m => m.ChangesComponent),
    providers: [
      provideEffects(ChangesEffects),
      ChangeService,
      provideState({name: changes.changeReducerFeatureKey, reducer: changes.reducer})
    ]
  },
  {
    path: '**',
    component: AppComponent
  }];

const authConfig: PassedInitialConfig = {
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
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideStore({ "user": UserReducer }),
    provideEffects([]),
    provideRouter(routes),
    provideAuth(authConfig),
    provideHttpClient(withInterceptors([authInterceptor()])),
    provideAnimations(),
    { provide: LOCALE_ID, useValue: 'de-DE'}
  ]
};
