import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authenticatedGuard } from './authenticated.guard';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';

const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: 'home', component: HomeComponent },
    {
        path: 'start',
        loadChildren: () => import('./start/start.module').then(m => m.StartModule),
        canActivateChild: [authenticatedGuard]
    },
    {
        path: 'member',
        loadChildren: () => import('./member/member.module').then(m => m.MemberModule),
        canActivateChild: [authenticatedGuard]
    },
    { path: 'onboarding', loadChildren: () => import('./on.boarding/on.boarding.module').then(m => m.OnBoardingModule) },
    {
        path: '**',
        component: AppComponent

    }];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
