import { Injectable, inject } from '@angular/core';
import { Observable, first, map, take } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Member } from '../model/Member';


@Injectable()
export class MemberService {

    private readonly http = inject(HttpClient);
    private readonly memberBasePath: string = "/api/v1/member/";

    getMemberById(memberId: string): Observable<Member> {
        const loadUrl: string = `${this.memberBasePath}${memberId}`;
        return this.http.get<Member>(loadUrl).pipe(take(1));
    }

    storeMember(memberId: string, member: Member): Observable<void | Error> {
        const postUrl = `${this.memberBasePath}${memberId}`;
        const headers = new HttpHeaders({ "Content-type": "application/json", "Accept": "application/json" });
        return this.http.put<Member>(postUrl, member, { headers: headers })
            .pipe(first(), map(_ => { }));
    }
}
