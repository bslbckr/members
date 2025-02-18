import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthenticationService } from '../authentication.service';

@Component({
    templateUrl: './redirect.component.html',
    styleUrls: ['./redirect.component.css'],
    standalone: false
})
export class RedirectComponent implements OnInit, OnDestroy {

    readonly router = inject(Router);
    readonly foo = inject(ActivatedRoute);
    readonly authService = inject(AuthenticationService);

    private paramSubscription?: Subscription;
    ngOnInit() {
        this.paramSubscription = this.foo.queryParamMap.subscribe(this.readQueryParams);
    }

    ngOnDestroy() {
        this.paramSubscription?.unsubscribe();
    }

    private readQueryParams(_: ParamMap) {
    }
}
