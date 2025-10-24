import { TestBed } from '@angular/core/testing';

import { OnBoardingService } from './on-boarding.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('OnBoardingService', () => {
  let service: OnBoardingService;

  beforeEach(() => {
    TestBed.configureTestingModule({

      providers: [provideHttpClient(), provideHttpClientTesting(), OnBoardingService]
    });
    service = TestBed.inject(OnBoardingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
