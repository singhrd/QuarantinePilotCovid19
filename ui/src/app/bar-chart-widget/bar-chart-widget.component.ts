import { Component, OnInit } from '@angular/core';
import 'anychart';
import { CovidReportService } from '../services/covid-report.service';
import { ResultMessage } from '../models/data-types';

@Component({
  selector: 'app-bar-chart-widget',
  templateUrl: './bar-chart-widget.component.html',
  styleUrls: ['./bar-chart-widget.component.scss'],
})
export class BarChartWidgetComponent implements OnInit {

  // TODO make into dropdown for location
  locations = ['San Diego', 'New York'];
  selectedLocation = 'San Diego';
  dataSet = [];
  dataLabels = ['Date', 'Confirmed', 'Deaths', 'Active', 'Recovered'];

  constructor( private service: CovidReportService ) { }

  /**
   * Populate the data set for for a given location.
   */
  populateDataSet(): void {
    const displayData = [];
    this.service.getCovidResults().subscribe((res: ResultMessage) => {
        if (res.snapshots) {
          // Filter based on location
          const filteredData = res.snapshots.filter(item => item.province_state === this.selectedLocation);
          console.log(filteredData);

          filteredData.forEach(entry => {
            displayData.push([entry.date, entry.confirmed, entry.deaths, entry.active, entry.recovered]);
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

  ngOnInit(): void {
    this.populateDataSet();
  }

}
