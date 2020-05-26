import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
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

  // Dropdown options
  locations = LocationsByCountryName;
  availableLocations = [];
  selectedLocations: Array<string> = [];

  windowsAvailable = ['daily', 'weekly', 'biweekly', 'triweekly'];
  selectedWindow = this.windowsAvailable[1];
  metrics = ['spread rate', 'daily growth rate', 'fatality rate', 'epidemic control ratio'];

  selectedMetric = this.metrics[0];

  chartDescriptions = [
    'Rate of growth of cumulative confirmed cases. Similar to rho.',
    'Ratio of daily confirmed cases over successive days. Value less than 1 for a sustained period indicates inflection point (peak) has been reached.',
    'Percentage deaths within confirmed cases. Flu fatality rate is 0.001.',
    'Ratio of daily confirmed cases to threshold per capita. Value less than 1 for 3 weeks implies the epidemic is under control'
  ];
  chartDescriptionText = this.chartDescriptions[0];
  noChartData = true;

  /**
   * Constructor
   * @param service 
   */
  constructor(private service: CovidReportService) {
    this.createMultiselectLabels();
  }

  /**
   * Update selected window to view, redraw graph
   * @param day - window to get the moving average for
   */
  selectWindow(window: string) {
    this.selectedWindow = window;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric);
  }

  /**
   * Update selected metric, redraw graph
   * @param metric - metric type
   */
  selectMetric(metric: string, idx: number) {
    this.selectedMetric = metric;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric);
    this.chartDescriptionText = this.chartDescriptions[idx];
  }

  /**
   * Populate the data set to be used for the chart, and then call the chart generation function
   * @param locations - string array of locations to chart
   * @param window - window to view the moving average
   * @param metric - metric type to chart
   */
  getData(locations: Array<string>, window: string, metric: string) {
    this.noChartData = true;
    const displayData = [];
    const requestArray = [];
    const labels = [];

    // Skip empty data sets
    if (locations.length === 0) {
      return;
    }

    // Reset chart data, create list of service calls for all selected locations
    this.noChartData = false;
    locations.forEach(loc => {
      requestArray.push(this.service.getAnnotations(loc));
      labels.push(loc);
    });

    // Wait for all service calls to return before proceeding
    forkJoin(requestArray)
      // .takeWhile(() => this.alive)
      .subscribe(allResponses => {
        // Loop over all responses (one per selected location)
        for (let i = 0; i < allResponses.length; i++) {
          const res: any = allResponses[i];
          let arr = new Array<number>(allResponses.length);
          if (res.elements) {
            // Loop over each date of data within each location
            res.elements.forEach((entry, j) => {
              // Plot the selected metric
              let metricData = entry.movingAverageGrowthRate;
              if (metric === 'spread rate') {
                metricData = entry.movingAverageEstimatedAlpha;
              }
              if (metric === 'fatality rate') {
                metricData = entry.movingAverageDeathRate;
              }
              if (metric === 'epidemic control ratio') {
                metricData = entry.movingAverageControlAssessment;
              }
              // Grab the value based on selected time period
              metricData.filter(windowInfo => {
                if (windowInfo[0] === window) {
                  if (i === 0) {
                    // Push initial label and empty arrays for data
                    // 2D array that is organized by:
                    // [ date1, location1, location2, location3, ... ]
                    // [ date2, location1, location2, location3, ... ]
                    // [ date3, location1, location2, location3, ... ]
                    let arr2 = [entry.date].concat(arr);
                    displayData.push(arr2);
                  }
                  // Set value for the array in the column based on location index
                  displayData[j][i + 1] = windowInfo[1];
                }
              });
            });
          }
        }
        // Generate the chart
        this.generateChart(displayData, labels);

      }, err => {
        // TODO
      });
  }

  /**
   * Generate the line chart with given input data set.
   * @param data - input data set
   * @param labels - labels for data set
   */
  generateChart(data, labels) {
    this.destroyChart();
    this.chart = anychart.line();

    const dataSet = anychart.data.set(data);
    const seriesData = [];
    const series = [];

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

    // set yAxis title in title case
    const title = this.selectedMetric.replace(/\b[a-z]/g, (x) => x.toLocaleUpperCase());
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

  /**
   * Set the multi-select options based on the contry list.
   */
  createMultiselectLabels() {
    const locationsMultiselect = [];
    this.locations.forEach(country => {
      locationsMultiselect.push({ label: country, value: country });
    });
    this.availableLocations = locationsMultiselect;
  }

  /**
   * Hide the multiselect panel, kick off chart update.
   */
  hidePanel() {
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric);
  }

  /**
   * Destroy an existing chart before re-writing.
   */
  destroyChart(): void {
    if (this.chart) {
      if (this.chart.container()) {
        this.chart.container().remove();
      }
      this.chart = null;
    }
  }

  /**
   * Init lifecycle hook
   */
  ngOnInit(): void {
    this.alive = true;
  }

}
