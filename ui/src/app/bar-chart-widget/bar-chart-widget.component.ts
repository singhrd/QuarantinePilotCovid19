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

  // TODO make into dropdown for location
  locations = LocationsByCountryName;
  snapshots = ['daily','cumulative'];
  normalizations = ['absolute','per-capita'];
  
  selectedLocation = 'US';
  selectedSnapshot = this.snapshots[0];
  selectedNormalization = this.normalizations[1];
  
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
        // Filter based on location
        // const filteredData = res.snapshots.filter(item => item.country === this.selectedLocation);

        res.snapshots.forEach(entry => {
          if(this.selectedNormalization === 'absolute')
            displayData.push([entry.date, entry.active, entry.recovered,entry.deaths]);
          else
            displayData.push([entry.date, entry.activeNormalized, entry.recoveredNormalized,entry.deathsNormalized]);
        });
        this.dataSet = displayData;
      }
    });

  }

  selectLocation(loc: string) {
    console.log('new location:', loc);
    this.selectedLocation = loc;
    this.populateDataSet();
  }
  
  selectSnapshot(snapshot: string) {
    console.log('new snapshot:', snapshot);
    this.selectedSnapshot = snapshot;
    this.populateDataSet();
  }

  selectNormalization(normalization: string) {
    console.log('new normalization:', normalization);
    this.selectedNormalization = normalization;
    this.populateDataSet();
  }
  
  ngOnInit(): void {
    this.populateDataSet();
  }

}
