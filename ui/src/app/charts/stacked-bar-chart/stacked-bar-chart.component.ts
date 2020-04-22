import { Component, OnInit, OnDestroy, OnChanges, ViewChild, ViewEncapsulation, Input } from '@angular/core';
import { ResultsPerDate } from '../../models/data-types';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class StackedBarChartComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('container') container;

  @Input() inputDataSet: any[];

  // widget vars
  alive = false;

  // data set vars
  data: Array<ResultsPerDate> = [];
  dataSet: anychart.data.Set = null;

  // chart vars
  chart: anychart.charts.Cartesian = null;

  /**
   * Component constructor.
   */
  constructor() { }

  /**
   * Load chart data into Set type from input data
   */
  loadChartData(): void {
    this.dataSet = anychart.data.set(this.inputDataSet);

  }

  /**
   * Generate chart element
   */
  generateChart(): void {
    this.loadChartData();
    // map data for the first series, take x from the zero column and value from the first column of data set
    const seriesData1 = this.dataSet.mapAs({ x: 0, value: 1, label: 'Confirmed' });
    const seriesData2 = this.dataSet.mapAs({ x: 0, value: 2, label: 'Deaths' });
    const seriesData3 = this.dataSet.mapAs({ x: 0, value: 3, label: 'Recovered' });
    const seriesData4 = this.dataSet.mapAs({ x: 0, value: 4, label: 'Active' });

    this.chart = anychart.column();
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

  destroyChart(): void {
    if (this.chart) {
      if (this.chart.container()) {
        this.chart.container().remove();
      }
      this.chart = null;
    }
  }

  ngOnInit(): void {
    this.alive = true;
  }

  ngOnChanges(): void {
    this.destroyChart();
    this.generateChart();
  }

  ngOnDestroy(): void {
    this.alive = false;
  }

}
