import { TestBed } from '@angular/core/testing';

import { CovidReportService } from './covid-report.service';

describe('CovidReportService', () => {
  let service: CovidReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CovidReportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
