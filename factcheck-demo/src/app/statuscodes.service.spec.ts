import { TestBed, inject } from '@angular/core/testing';

import { StatuscodesService } from './statuscodes.service';

describe('StatuscodesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StatuscodesService]
    });
  });

  it('should be created', inject([StatuscodesService], (service: StatuscodesService) => {
    expect(service).toBeTruthy();
  }));
});
