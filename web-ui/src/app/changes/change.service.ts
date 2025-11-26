import { HttpClient, httpResource, HttpResourceRef } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { first, map, Observable } from 'rxjs';
import { StateChanges } from './state.changes';
import { EntriesOrCancellationsData, EntriesOrCancellation } from './entriesOrCancellations';

@Injectable()
export class ChangeService {
  private readonly baseUrl = "/api/v1/changes/";
  private readonly stateChangeUrl = `${this.baseUrl}state`;
  private readonly entriesUrl = `${this.baseUrl}entries`;
  private readonly cancellationsUrl = `${this.baseUrl}cancellation`;
  constructor() { }

  private readonly http = inject(HttpClient);

  loadChanges(): Observable<StateChanges[]> {
    return this.http.get<StateChanges[]>(this.stateChangeUrl, { responseType: 'json' }).pipe(first());
  }

  loadCancellations(): Observable<EntriesOrCancellation[]> {
    return this.http.get<EntriesOrCancellationsData[]>(this.cancellationsUrl, { responseType: 'json' })
      .pipe(
        first(),
        map(x => x.map(eod => new EntriesOrCancellation(eod))));
  }

  loadEntries(): Observable<EntriesOrCancellation[]> {
    return this.http.get<EntriesOrCancellationsData[]>(this.entriesUrl, { responseType: 'json' })
      .pipe(
        first(),
        map(x => x.map(eod => new EntriesOrCancellation(eod))));
  }

  getEntries(): HttpResourceRef<EntriesOrCancellation[]> {
    return httpResource(() => this.entriesUrl, {
      parse: this.parseUnkown,
      defaultValue: []
    });
  }
  
  getCancellations() : HttpResourceRef<EntriesOrCancellation[]> {
    return httpResource(()=>this.cancellationsUrl, {
      parse: this.parseUnkown,
      defaultValue: []
    });
  }

  getChanges() : HttpResourceRef<StateChanges[]> {
    return httpResource(()=>this.stateChangeUrl, {
      defaultValue: []
    });
  }
  
  private parseUnkown(data: unknown): EntriesOrCancellation[] {
    const arr = data as EntriesOrCancellationsData[];
      return arr.map(d => new EntriesOrCancellation(d));
  }
}
