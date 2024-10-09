import { Component, OnInit, inject } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Store } from '@ngrx/store';
import { LoadMemberActions } from '../actions/load-member.actions';
import { ModifyMemberActions } from '../actions/modify-member.actions';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { StoreSelectors, MemberSelectors } from '../selectors/user.selector';
import { StepperOrientation } from '@angular/cdk/stepper';
import { filter, map, take, tap, withLatestFrom } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Member } from '../model/Member';
import { differenceInCalendarDays } from 'date-fns/fp';
import { endOfMonth, format } from 'date-fns';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { CancellationDialogComponent, CancellationDialogData } from './cancellation-dialog/cancellation-dialog.component';
import { ResourceSelectors } from '../selectors/resources.selectors';
import { UserSelectors } from '../selectors/user.selectors';

@Component({
    selector: 'app-member',
    templateUrl: './member.component.html',
    styleUrls: ['./member.component.css']
})
export class MemberComponent implements OnInit {

    private readonly store = inject(Store);
    private readonly builder = inject(FormBuilder);
    private readonly breakpoint = inject(BreakpointObserver);
    private readonly snackBar = inject(MatSnackBar);
    private readonly dialog = inject(MatDialog);
    private readonly selectedMemberId$ = this.store.select(ResourceSelectors.selectMemberId)
        .pipe(takeUntilDestroyed());
    readonly masterDataUpdated$ = this.store.select(MemberSelectors.dataUpdated)
        .pipe(takeUntilDestroyed());
    private readonly nextEndOfQuarter: Date[];
    private memberId: string = "";
    private isOnboarding = false;
    orientation: StepperOrientation = "horizontal";

    readonly forms = this.builder.nonNullable.group({
        master: this.builder.nonNullable.group({
            name: ['', Validators.required],
            givenName: ['', Validators.required],
            dayOfBirth: [new Date(), Validators.required],
            entryDate: [new Date(), Validators.required],
            exitDate: new FormControl<Date | null>(null)
        }),
        status: this.builder.nonNullable.group({
            status: ['', Validators.required],
            statusEffective: new FormControl<Date | null>(null, Validators.required)
        }),
        dfv: this.builder.nonNullable.group({
            dfvNumber: [0],
            dse: [false, Validators.required],
            discount: [false, Validators.required],
            gender: ['', Validators.required],
        }),
        contact: this.builder.nonNullable.group({
            street: ['', Validators.required],
            city: ['Potsdam', [Validators.required, Validators.minLength(3)]],
            zipCode: ['14471', [Validators.required, Validators.minLength(5), Validators.maxLength(5)]],
            email: ['', [Validators.required, Validators.email]],
            emailList: [true, Validators.required],
            phone: ['', Validators.required]
        })
    });

    invalid$ = this.forms.statusChanges.pipe(takeUntilDestroyed(), map(stat => stat !== 'VALID'));

    constructor() {
        const today = new Date();
        const month = today.getMonth();
        const currentQuarter = Math.ceil((month + 1) / 3);
        let yearOffset = 0;
        const nextQuarters: Date[] = [];
        for (let count = 0; count < 5; count++) {
            let effectiveQuarter;
            if (currentQuarter + count <= 4) {
                //within current year
                effectiveQuarter = currentQuarter + count;
            } else {
                effectiveQuarter = currentQuarter + count - 4;
                yearOffset = 1;
            }
            nextQuarters.push(endOfMonth(new Date(today.getFullYear() + yearOffset, (effectiveQuarter * 3) - 1, 1)));
        }

        const lastDayOfCurrentQuarter = nextQuarters[0];
        const daysToEndOfQuarter = differenceInCalendarDays(today, lastDayOfCurrentQuarter);
        if (daysToEndOfQuarter < 14) {
            this.nextEndOfQuarter = nextQuarters.slice(1, 5);
        } else {
            this.nextEndOfQuarter = nextQuarters.slice(0, 4);
        }
    }
    private readonly bumms = this.forms.get("status.status")?.valueChanges
        .pipe(takeUntilDestroyed(),
            withLatestFrom(this.store.select(MemberSelectors.selectLoadedStatus)),
            map(([newStat, oldStat]) => newStat === oldStat))
        .subscribe((unchanged: boolean) => {
            const control = this.forms.get("status.statusEffective");
            if (control == null || control == undefined) { throw new Error("FormControl status.statusEffective is missing"); }
            if (unchanged) {
                control.disable();
            } else {
                control.enable({ emitEvent: true });
                control.reset(null);
            }
        });

    private readonly userChangedSub = this.store.select(MemberSelectors.selectMember)
        .pipe(takeUntilDestroyed(), filter(this.filterNullOrUndefined))
        .subscribe(mem => {
            this.forms.setValue({
                master: {
                    name: mem.name,
                    givenName: mem.givenName,
                    entryDate: mem.entryDate,
                    dayOfBirth: mem.dayOfBirth,
                    exitDate: mem.exitDate
                },
                status: {
                    status: mem.state,
                    statusEffective: mem.state === null ? mem.entryDate : mem.stateEffective
                },
                dfv: {
                    dfvNumber: mem.dfvNumber,
                    dse: mem.dse,
                    discount: mem.dfvDiscount,
                    gender: mem.gender
                },
                contact: {
                    street: mem.street,
                    zipCode: mem.zipCode,
                    city: mem.city,
                    phone: mem.mobile,
                    email: mem.email,
                    emailList: mem.emailList
                }
            });
            this.isOnboarding = mem.state === null;
        });
    private readonly userNeedsUpdate$ = this.store.select(MemberSelectors.updateNecessary)
        .pipe(takeUntilDestroyed(),
            filter(up => up === true),
            withLatestFrom(this.store.select(UserSelectors.currentUser)),
            map(([_, user]) => LoadMemberActions.updateMemberFromUser(
                {
                    name: user?.family_name ?? "",
                    givenName: user?.given_name ?? "",
                    email: user?.email ?? ""
                })))
        .subscribe((action) => this.store.dispatch(action));

    private readonly breakpointSub = this.breakpoint.observe(Breakpoints.Handset)
        .pipe(takeUntilDestroyed())
        .subscribe(state => {
            if (state.matches) {
                this.orientation = "vertical";
            } else {
                this.orientation = "horizontal";
            }
        });

    private readonly showSnackbarSuccess = this.store.select(StoreSelectors.success)
        .pipe(takeUntilDestroyed(), filter(x => x))
        .subscribe(_ => {
            this.snackBar.open("Änderungen erfolgreich gespeichert", "Ok", { duration: 5000 });
        });
    private readonly showSnackbarFailure = this.store.select(StoreSelectors.failure)
        .pipe(takeUntilDestroyed(), filter(x => x))
        .subscribe(_ => {
            this.snackBar.open("Änderungen konnten nicht gespeichert werden", "Ok", { duration: 5000 });
        });

    ngOnInit() {
        //this.memberId = this.route.snapshot.paramMap.get('memberId') || '';
        this.selectedMemberId$.pipe(filter(this.filterNullOrUndefined),
            take(1),
            tap(id => this.memberId = id))
            .subscribe(id => this.store.dispatch(LoadMemberActions.loadLoadMembers({ memberId: id })));

    }

    private filterNullOrUndefined<T>(x: T | null | undefined): x is T {
        return typeof x !== 'undefined' && x !== null;
    }

    private toMember(): Member {
        const raw = this.forms.getRawValue();
        return {
            id: this.memberId,
            name: raw.master.name,
            givenName: raw.master.givenName,
            gender: raw.dfv.gender as "male" | "female",
            dayOfBirth: raw.master.dayOfBirth,
            entryDate: raw.master.entryDate,
            exitDate: raw.master.exitDate,
            state: raw.status.status as "passiv" | "jugendliche" | "ermäßigt" | "berufstätig",
            stateEffective: raw.status.statusEffective ?? new Date(),
            dfvNumber: raw.dfv.dfvNumber,
            dfvDiscount: raw.dfv.discount,
            dse: raw.dfv.dse,
            street: raw.contact.street,
            zipCode: raw.contact.zipCode,
            city: raw.contact.city,
            mobile: raw.contact.phone,
            email: raw.contact.email,
            emailList: raw.contact.emailList
        };
    }

    get allowedChangeDates(): { date: Date, description: string }[] {
        const stateEffective = this.forms.get('status.stateEffective')?.value ?? new Date();
        return this.isOnboarding ?
            [{ date: stateEffective, description: format(stateEffective, "d.M.yyy") }] :
            this.nextEndOfQuarter.map(d => {
                return {
                    date: d, description: format(d, "d.M.yyy")
                };
            });
    }

    get onBoardingMode() { return this.isOnboarding; }

    openCancellationDialog() {
        const dialogData: CancellationDialogData = {
            allowedCancellationDate: this.nextEndOfQuarter,
            selectedDate: null
        };
        const dialogRef = this.dialog.open<CancellationDialogComponent, CancellationDialogData, Date>(CancellationDialogComponent,
            {
                data: dialogData,
                ariaLabel: "Mitgliedschaft kündigen",
                ariaModal: true,
                hasBackdrop: true,
                disableClose: true,
                autoFocus: true
            });
        dialogRef.afterClosed()
            .pipe(filter(this.filterNullOrUndefined),
                map(d => {
                    const member = this.toMember();
                    member.exitDate = d;
                    return member;
                }),
                map(m => ModifyMemberActions.store({ member: m })))
            .subscribe(action => {
                this.store.dispatch(action);
            });
    }

    revert() {
        this.store.dispatch(ModifyMemberActions.revert());
    }
    update() {
        this.store.dispatch(ModifyMemberActions.store({ member: this.toMember() }));
    }
}
