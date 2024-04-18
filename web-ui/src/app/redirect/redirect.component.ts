import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthenticationService } from '../authentication.service';

@Component({
    templateUrl: './redirect.component.html',
    styleUrls: ['./redirect.component.css']
})
export class RedirectComponent implements OnInit {

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

    private readQueryParams(params: ParamMap) {
    }
}
