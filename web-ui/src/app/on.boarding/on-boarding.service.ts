import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

@Injectable()
export class OnBoardingService {

    private readonly http = inject(HttpClient);
    private readonly onBoardingUrl = "/api/v1/onboarding";

    submit(member: NewMember): Observable<void> {
        const headers = new HttpHeaders({ "Content-type": "application/json", "Accept": "application/json" });

        return this.http.post(this.onBoardingUrl, member, { headers: headers }).pipe(map(_ => { }));
    }
}

export interface NewMember {
    firstName: string;
    name: string;
    login: string;
    email: string;
    memberIsChild: boolean;
    memberFirstName: string;
    memberName: string;
}
