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
  ) {
    // console.log('constructor');
    // this.loadCovidResults().subscribe(res => {
    //   if (res.snapshots) {
    //     console.log(res.snapshots);
    //     this.data = res.snapshots;
        // this.data = res.snapshots.filter;
        // console.log(this.data);
    //   }
    // });
  }

  getResultsByCity(city: string): ResultsPerDate[] {
    this.loadCovidResults().subscribe((res: ResultMessage) => {
      if (res.snapshots) {
        return res.snapshots.filter(item => item.province_state === city);
      }
    });
    return null;
  }

  // }
  printResults() {
    return this.data;
  }

  loadCovidResults() {
    return this.http.get('/assets/sampleCovidSnapshot.json');
  }
}
