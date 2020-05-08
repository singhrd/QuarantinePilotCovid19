import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResultsPerDate, ResultMessage } from '../models/data-types';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CovidReportService {

  // data = [];

  data: Array<ResultsPerDate> = [];

  constructor(
    private http: HttpClient
  ) { }

  // getResultsByCity(city: string): ResultsPerDate[] {
  //   this.loadCovidResults().subscribe((res: ResultMessage) => {
  //     if (res.snapshots) {
  //       return res.snapshots.filter(item => item.province_state === city);
  //     }
  //   });
  // }

  printResults() {
    return this.data;
  }

  getCovidResults(location: string) {
    return this.http.get('/assets/cumulative-snapshots/' + location + '_CumulativeSnapshots.json');
  }

  getAnnotations(location: string) {
    return this.http.get('/assets/anno2.json');
  }
}
