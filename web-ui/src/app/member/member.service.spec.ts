import { TestBed } from '@angular/core/testing';

import { MemberService } from './member.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('MemberService', () => {
  let service: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MemberService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MemberService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
