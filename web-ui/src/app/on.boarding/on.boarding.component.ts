import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { filter, map } from 'rxjs';
import { onBoardingActions } from '../actions/actions';
import { onboardingSelectors } from '../selectors/selectors';
import { MatSnackBar } from '@angular/material/snack-bar';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';


@Component({
    selector: 'app-on.boarding',
    templateUrl: './on.boarding.component.html',
    styleUrls: ['./on.boarding.component.css']
})
export class OnBoardingComponent {

    private readonly memberFirstName = new FormControl({ value: "", disabled: true }, Validators.required);
    private readonly memberName = new FormControl({ value: "", disabled: true }, Validators.required);
    private readonly isChild = new FormControl(false, [Validators.required]);

    readonly form = new FormGroup({
        firstName: new FormControl("", Validators.required),
        name: new FormControl("", Validators.required),
        login: new FormControl("", [Validators.required, Validators.minLength(3)]),
        email: new FormControl("", [Validators.email, Validators.required]),
        isChild: this.isChild,
        memberFirstName: this.memberFirstName,
        memberName: this.memberName
    });
    private readonly store = inject(Store);
    private readonly snackBar = inject(MatSnackBar);

    readonly invalid$ = this.form.statusChanges.pipe(takeUntilDestroyed(), map(s => s !== 'VALID'));
    private readonly onBoardingSuccess = this.store.select(onboardingSelectors.success)
        .pipe(takeUntilDestroyed(), filter(x => x === true))
        .subscribe(_ => this.snackBar.open("OnBoarding erfolgreich", "Fein", { duration: 5000 }));
    private readonly onBoardingFailure = this.store.select(onboardingSelectors.failure)
        .pipe(takeUntilDestroyed(), filter(x => x === true))
        .subscribe(_ => this.snackBar.open("OnBoarding fehlgeschlagen", "Schade", { duration: 5000 }));
    private readonly isChildChanged = this.isChild.valueChanges
        .pipe(takeUntilDestroyed())
        .subscribe(v => {
            if (v) {
                this.memberFirstName.enable();
                this.memberName.enable();
            } else {
                this.memberFirstName.disable();
                this.memberName.disable();
            }
        });
    send(): void {
        const firstName = this.form.get("firstName")?.value ?? "";
        const name = this.form.get("name")?.value ?? "";
        const login = this.form.get("login")?.value ?? "";
        const email = this.form.get("email")?.value ?? "";
        const isChild = this.isChild.value ?? false;
        const memberFirstName = isChild ? this.memberFirstName.value ?? "" : "";
        const memberName = isChild ? this.memberName.value ?? "" : "";
        this.store.dispatch(onBoardingActions.start(
            {
                firstname: firstName,
                name: name,
                login: login,
                email: email,
                isChild: isChild,
                memberFirstName: memberFirstName,
                memberName: memberName
            }));
    }
}


