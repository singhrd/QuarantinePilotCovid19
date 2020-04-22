import { Component, OnInit } from '@angular/core';
import 'anychart';
import { CovidReportService } from '../services/covid-report.service';

@Component({
  selector: 'app-bar-chart-widget',
  templateUrl: './bar-chart-widget.component.html',
  styleUrls: ['./bar-chart-widget.component.scss'],
})
export class BarChartWidgetComponent implements OnInit {

  data;
  dataSet = [
    ['01/31/20', 12814, 3054, 4376, 4229],
    ['02/01/20', 13012, 5067, 3987, 3932],
    ['02/02/20', 11624, 7004, 3574, 5221],
    ['02/03/20', 8814, 9054, 4376, 9256],
    ['02/04/20', 12998, 12043, 4572, 3308],
    ['02/05/20', 12321, 15067, 3417, 5432],
    ['02/06/20', 10342, 10119, 5231, 13701],
    ['02/07/20', 22998, 12043, 4572, 4008],
    ['02/08/20', 11261, 10419, 6134, 18712],
    ['02/09/20', 10261, 14419, 5134, 25712]
  ];

  constructor( private service: CovidReportService ) { }

  ngOnInit(): void {
    this.data = this.service.getResultsByCity('San Diego');
  }

}
