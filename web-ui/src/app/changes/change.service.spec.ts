import { TestBed } from '@angular/core/testing';

import { ChangeService } from './change.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('ChangeService', () => {
  let service: ChangeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ChangeService, provideHttpClient(), provideHttpClientTesting()] 
    });
    service = TestBed.inject(ChangeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
