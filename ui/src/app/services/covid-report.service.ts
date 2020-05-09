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

  getCovidResults(location: string, snapshot: string) {
    if(snapshot === 'daily')
      return this.http.get('/assets/snapshots/daily-snapshots/' + location + '_DailySnapshots.json');
    else
      return this.http.get('/assets/snapshots/cumulative-snapshots/' + location + '_CumulativeSnapshots.json');
  }
  

  getAnnotations(location: string) {
    return this.http.get('/assets/annotations/' + location + '_Annotations.json');
  }
}
