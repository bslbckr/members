import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map, first } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class SendMailService {

  private readonly http = inject(HttpClient);
  private readonly sendUrl: string = 'api/v1/email/';

  send(subject: string, body: string):Observable<boolean>  {
    return this.http.post(this.sendUrl,
      {subject: subject, body: body},
      {headers: {'Content-type': 'application/json'}, observe: 'response'})
      .pipe(first(), map(r => r.status >= 200 && r.status < 300));
  }
}
