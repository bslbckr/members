import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { LoadMemberActions } from '../../actions/load-member.actions';
import { catchError, concatMap, map, switchMap, tap } from 'rxjs';
import { MemberService } from '../member.service';
import { ModifyMemberActions } from '../../actions/modify-member.actions';
import { of } from 'rxjs';
import { Router } from '@angular/router';


@Injectable()
export class MemberEffects {

    private readonly actions$ = inject(Actions);
  private readonly service = inject(MemberService);
  private readonly router = inject(Router);
    constructor() { }

    loadMember$ = createEffect(() => {
        return this.actions$.pipe(ofType(LoadMemberActions.loadLoadMembers),
            switchMap(act => this.service.getMemberById(act.memberId)),
            map(mem => LoadMemberActions.loadLoadMembersSuccess({ member: mem })))
    });

    updateMember$ = createEffect(() => {
        return this.actions$.pipe(
            ofType(ModifyMemberActions.store),
            concatMap(act => this.service.storeMember(act.member.id, act.member)
                .pipe(map(_ => ModifyMemberActions.storeSuccess()),
                    catchError(err => of(ModifyMemberActions.storeFailed({ error: err as Error }))))))
    });

  navigateToStart$ = createEffect(() => {
  return this.actions$.pipe(
    ofType(ModifyMemberActions.navigateToStart),
    tap(_ => this.router.navigate(["/start"])))
    }, {dispatch: false});
}
