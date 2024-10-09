import { LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { RedirectComponent } from './redirect/redirect.component';
import { AuthConfigModule } from './auth/auth-config.module';
import { HomeComponent } from './home/home.component';
import { MenuComponent } from './menu/menu.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from 'angular-auth-oidc-client';
import { MatDividerModule } from '@angular/material/divider';
import { UserReducer } from './reducers/user.reducer';
import { ResourceReducer } from './reducers/resource.reducer';
import { MemberReducer } from './reducers/member.reducer';
import { OnBoardingReducer } from './reducers/reducers';

@NgModule({
    declarations: [
        AppComponent,
        RedirectComponent,
        HomeComponent,
        MenuComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        StoreModule.forRoot({ "user": UserReducer, "resource": ResourceReducer, "member": MemberReducer, "onBoarding": OnBoardingReducer }),
        EffectsModule.forRoot([]),
        AuthConfigModule,
        MatToolbarModule,
        MatDividerModule,
        MatButtonModule,
        MatIconModule
    ],
    providers: [
        { provide: LOCALE_ID, useValue: 'de-DE' },
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
