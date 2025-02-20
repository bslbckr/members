import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, take } from 'rxjs';

export interface MemberOverview {
  givenName: string;
  name: string;
  state: string;
  stateEffective: Date;
   entryDate: Date;
  exitDate: Date;
}


@Injectable({
  providedIn: 'root'
})
export class MemberOverviewService {
  private readonly overviewUrl:string = "/api/v1/member";
  private readonly http = inject(HttpClient);

  loadMemberOverview():Observable<MemberOverview[]> {
    return this.http.get<MemberOverview[]>(this.overviewUrl).pipe(take(1));
  }
}
