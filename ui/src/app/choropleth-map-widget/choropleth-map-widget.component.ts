import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { CovidReportService } from '../services/covid-report.service';
import { forkJoin } from 'rxjs';
import { serializeNodes } from '@angular/compiler/src/i18n/digest';

@Component({
  selector: 'app-choropleth-map-widget',
  templateUrl: './choropleth-map-widget.component.html',
  styleUrls: ['./choropleth-map-widget.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ChoroplethMapWidgetComponent implements OnInit {
  @ViewChild('mapContainer') mapContainer;


  // Widget vars
  alive = false;
  
  /**
   * Constructor
   * @param service 
   */
  constructor(private service: CovidReportService) {
  
  }


  /**
   * Init lifecycle hook
   */
  ngOnInit(): void {
    this.alive = true;
//    this.getData();
  }

}
