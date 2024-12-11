import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { first, Observable } from 'rxjs';
import { StateChanges } from './state.changes';

@Injectable()
export class ChangeService {

  constructor() { }

  private readonly http = inject(HttpClient);

  loadChanges():Observable<StateChanges[]> {
    return this.http.get<StateChanges[]>("/api/v1/changes/state",{responseType: 'json'}).pipe(first());
  }
}
