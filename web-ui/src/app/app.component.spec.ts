import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { provideMockStore } from '@ngrx/store/testing';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import {of} from 'rxjs';

describe('AppComponent', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [provideMockStore(), {provide: OidcSecurityService, useValue: { checkAuth() {return of( true);}}}],
    imports: [RouterTestingModule, AppComponent]
}));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'guc-members'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('GUC Mitgliederverwaltung');
  });

});
