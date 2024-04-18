import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, take } from 'rxjs';
import { ResourceInfo } from '../model/ResourceInfo';

@Injectable()
export class ResourceService {
    private readonly http = inject(HttpClient);
    private readonly configUrl: string = "/api/v1/configuration";

    constructor() { }

    getResourcesForUser(): Observable<ResourceInfo[]> {
        return this.http.get<ResourceInfo[]>(this.configUrl).pipe(take(1));
    }
}
