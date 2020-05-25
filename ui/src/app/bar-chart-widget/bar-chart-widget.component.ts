import { Component, OnInit, ViewEncapsulation, Input } from '@angular/core';
import { CovidReportService } from '../services/covid-report.service';
import { ResultMessage, LocationsByCountryName } from '../models/data-types';

@Component({
  selector: 'app-bar-chart-widget',
  templateUrl: './bar-chart-widget.component.html',
  styleUrls: ['./bar-chart-widget.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class BarChartWidgetComponent implements OnInit {

  @Input() uuid: string;

  locations = LocationsByCountryName;
  snapshots = ['daily', 'cumulative'];
  normalizations = ['absolute', 'per-capita'];
  yAxisTitles = ['Cases', 'Cases per capita'];

  selectedLocation = 'US';
  selectedSnapshot = this.snapshots[0];
  selectedNormalization = this.normalizations[1];
  yAxisTitle = this.yAxisTitles[1];

  dataSet = [];
  dataLabels = ['Date', 'Active', 'Recovered', 'Deaths'];

  constructor(private service: CovidReportService) { }

  /**
   * Populate the data set for for a given location.
   */
  populateDataSet(): void {
    const displayData = [];
    this.service.getCovidResults(this.selectedLocation, this.selectedSnapshot).subscribe((res: ResultMessage) => {
      if (res.snapshots) {
        res.snapshots.forEach(entry => {
          if (this.selectedNormalization === 'absolute') {
            displayData.push([entry.date, entry.active, entry.recovered, entry.deaths]);
          } else {
            displayData.push([entry.date, entry.activeNormalized, entry.recoveredNormalized, entry.deathsNormalized]);
          }
        });
        this.dataSet = displayData;
      }
    });

  }

  /**
   * Location dropdown callback
   * @param loc - selected location
   */
  selectLocation(loc: string) {
    this.selectedLocation = loc;
    this.populateDataSet();
  }

  /**
   * Snapshot type dropdown callback
   * @param snapshot - selected snapshot
   */
  selectSnapshot(snapshot: string) {
    this.selectedSnapshot = snapshot;
    this.populateDataSet();
  }

  /**
   * Normalization dropdown callback
   * @param normalization - selected normalization
   */
  selectNormalization(normalization: string, idx: number) {
    this.selectedNormalization = normalization;
    this.yAxisTitle = this.yAxisTitles[idx];
    this.populateDataSet();
  }

  /**
   * Init lifecycle hook
   */
  ngOnInit(): void {
    this.populateDataSet();
  }

}
