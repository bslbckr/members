import { TestBed } from '@angular/core/testing';

import { MemberOverviewService } from './member-overview.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('MemberOverviewService', () => {
  let service: MemberOverviewService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MemberOverviewService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
