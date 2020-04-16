import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class StackedBarChartComponent implements OnInit {
  @ViewChild('container') container;

  dataSet = anychart.data.set([
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
  ]);

  // bar chart
  chart = anychart.column();

  constructor() { }

  generateChart() {
    // map data for the first series, take x from the zero column and value from the first column of data set
    const seriesData1 = this.dataSet.mapAs({ x: 0, value: 1 });
    const seriesData2 = this.dataSet.mapAs({ x: 0, value: 2 });
    const seriesData3 = this.dataSet.mapAs({ x: 0, value: 3 });
    const seriesData4 = this.dataSet.mapAs({ x: 0, value: 4 });

    this.chart.animation(true);
    this.chart.yScale().stackMode('value');

    // create first series with mapped data
    let series = this.chart.column(seriesData1).name('Confirmed');
    series = this.chart.column(seriesData2).name('Deaths');
    series = this.chart.column(seriesData3).name('Recovered');
    series = this.chart.column(seriesData4).name('Active');

    // turn on legend
    this.chart.legend().enabled(true).fontSize(13).padding([0, 0, 20, 0]);
    // set yAxis labels formatter
    this.chart.yAxis().labels().format('{%Value}{groupsSeparator: }');

    // set titles for axes
    this.chart.xAxis().title('Date');
    // this.chart.yAxis().title('Revenue in Dollars');

    // set interactivity hover
    this.chart.interactivity().hoverMode('by-x');

    this.chart.tooltip()
      .displayMode('union');

    // set container id for the chart
    this.chart.container('container');

    // initiate chart drawing
    this.chart.draw();
  }

  ngOnInit(): void {
    this.generateChart();
  }

}
