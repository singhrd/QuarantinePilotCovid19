import { Component, OnInit, OnChanges, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { LocationsByCountryName } from '../models/data-types';
import { CovidReportService } from '../services/covid-report.service';
import { forkJoin } from 'rxjs';
import { serializeNodes } from '@angular/compiler/src/i18n/digest';

@Component({
  selector: 'app-line-chart-widget',
  templateUrl: './line-chart-widget.component.html',
  styleUrls: ['./line-chart-widget.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LineChartWidgetComponent implements OnInit {
  @ViewChild('lineContainer') lineContainer;

  // Widget vars
  alive = false;

  // Chart vars
  chart: any = null;
  // Data set vars
  data: Array<any> = [];
  // dataSet2: anychart.data.Set = null;

  locations = LocationsByCountryName;
  selectedLocations: Array<string> = [];

  windowsAvailable = ['daily', 'weekly', 'biweekly', 'triweekly'];
  selectedWindow = this.windowsAvailable[1];
  metrics = ['spread rate', 'daily growth rate', 'fatality rate'];
  selectedMetric = this.metrics[0];
  noChartData = true;

  availableLocations = [];

  constructor(private service: CovidReportService) {
    this.createMultiselectLabels();
  }

  /**
   * Update selected window to view, redraw graph
   * @param day - window to get the moving average for
   */
  selectWindow(window: string) {
    console.log('new window:', window);
    this.selectedWindow = window;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric);
  }

  /**
   * Update selected metric, redraw graph
   * @param metric - metric type
   */
  selectMetric(metric: string) {
    console.log('new metric:', metric);
    this.selectedMetric = metric;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric);
  }

  /**
   * Populate the data set to be used for the chart, and then call the chart generation function
   * @param locations - string array of locations to chart
   * @param window - window to view the moving average
   * @param metric - metric type to chart
   */
  getData(locations: Array<string>, window: string, metric: string) {
    this.noChartData = true;
    let displayData = [];
    const requestArray = [];
    let labels = [];

    if (locations.length === 0) {
      return;
    }

    this.noChartData = false;
    locations.forEach(loc => {
      requestArray.push(this.service.getAnnotations(loc));
      labels.push(loc);
    });

    forkJoin(requestArray)
      // .takeWhile(() => this.alive)
      .subscribe(allResponses => {
        for (let i = 0; i < allResponses.length; i++) {
          const res: any = allResponses[i];
          let arr = new Array<number>(allResponses.length);
          if (res.elements) {
            res.elements.forEach((entry, j) => {
              let metricData = entry.movingAverageGrowthRate;
              if (metric === 'spread rate') {
                metricData = entry.movingAverageEstimatedAlpha;
              }
              if (metric === 'fatality rate') {
                metricData = entry.movingAverageDeathRate;
              }
              metricData.filter(windowInfo => {
                if (windowInfo[0] === window) {
                  if (i === 0) {
                    // push initial label and empty arrays for data
                    let arr2 = [entry.date].concat(arr);
                    displayData.push(arr2);
                  }
                  displayData[j][i + 1] = windowInfo[1];
                }
              });
            });
          }
        }

        this.generateChart(displayData, labels);

      }, err => {
        // TODO
       });
  }

  generateChart(data, labels) {
    this.destroyChart();
    this.chart = anychart.line();

    let dataSet = anychart.data.set(data);
    let seriesData = [];
    let series = [];

    // turn on chart animation
    this.chart.animation(true);

    // set chart padding
    this.chart.padding([10, 20, 5, 20]);

    // turn on the crosshair
    this.chart.crosshair()
      .enabled(true)
      .yLabel(false)
      .yStroke(null);

    // set tooltip mode to point
    this.chart.tooltip().positionMode('point');

    // set yAxis title
    const title = this.selectedMetric.charAt(0).toLocaleUpperCase() + this.selectedMetric.slice(1);
    this.chart.yAxis().title('Moving Average ' + title);
    this.chart.xAxis().labels().padding(5);

    // Map data per location
    labels.forEach((loc, col) => {
      // mat data set
      seriesData.push(dataSet.mapAs({ 'x': 0, 'value': col + 1 }));

      // create series data
      series.push(this.chart.line(seriesData[col]));
      series[col].name(loc);
      series[col].hovered().markers()
        .enabled(true)
        .type('circle')
        .size(4);
      series[col].tooltip()
        .position('right')
        .anchor('left-center')
        .offsetX(5)
        .offsetY(5);

    });

    // turn the legend on
    this.chart.legend()
      .enabled(true)
      .fontSize(13)
      .padding([0, 0, 10, 0]);

    // set container id for the chart
    this.chart.container('lineContainer');
    // initiate chart drawing
    this.chart.draw();
  }

  createMultiselectLabels() {
    let locationsMultiselect = [];
    this.locations.forEach(country => {
      locationsMultiselect.push({ label: country, value: country });
    });
    this.availableLocations = locationsMultiselect;
  }

  hidePanel() {
    console.log('panel is hidden!');
    this.getData(this.selectedLocations, 7, 'growth');
  }

  /**
   * Destroy an existing chart before re-writing.
   */
  destroyChart(): void {
    console.log('destroy chart line');
    if (this.chart) {
      if (this.chart.container()) {
        this.chart.container().remove();
      }
      this.chart = null;
    }
  }

  ngOnInit(): void {
    console.log('line init');
    this.alive = true;
    // this.destroyChart();
    // this.getData(['San Diego'], 7, 'growth');
    // this.renderer.setProperty(this.el.nativeElement, 'id', this.uuid);
  }

  ngOnChanges(): void {
    console.log('on changes line');
    // this.destroyChart();
    // this.getData(['San Diego'], 7, 'growth');
  }

  ngOnDestroy(): void {
    this.alive = false;
  }

}
