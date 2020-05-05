import { Component, OnInit } from '@angular/core';
import { LocationsByCountryName } from '../models/data-types';

@Component({
  selector: 'app-line-chart-widget',
  templateUrl: './line-chart-widget.component.html',
  styleUrls: ['./line-chart-widget.component.scss']
})
export class LineChartWidgetComponent implements OnInit {

  locations = LocationsByCountryName;
  selectedLocation = 'US';

  constructor() { }

  selectLocation(loc: string) {
    console.log('new location:', loc);
    this.selectedLocation = loc;
    // this.populateDataSet();
  }

  ngOnInit(): void {
  }

}
