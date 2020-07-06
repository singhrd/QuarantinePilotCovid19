import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { LocationsByLocaleName, CountriesByName, CountiesByName, StatesByName, SanDiegoZipCodesByName } from '../models/data-types';
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
  locations = LocationsByLocaleName;
  locationsCountries = CountriesByName;
  locationsCounties = CountiesByName;
  locationsStates = StatesByName;
  locationsZipCodesSanDiego = SanDiegoZipCodesByName;
  availableLocations = [];
  availableAlertsEmpty = [];

  selectedLocationsAll: Array<string> = ['US', 'California', 'San Diego,California'];
  selectedLocationsCountries: Array<string> = ['US', 'United Kingdom', 'India'];
  selectedLocationsCounties: Array<string> = ['San Diego,California', 'Riverside,California', 'Los Angeles,California'];
  selectedLocationsStates: Array<string> = ['Texas', 'California', 'Florida', 'Arizona'];
  selectedLocationsZipCodesSanDiegoCounty: Array<string> = ['92064', '91901', '92128'];

  windowsAvailable = ['daily', 'weekly', 'triweekly'];
  selectedWindow = this.windowsAvailable[1];

  localesAvailable = ['Countries', 'US States', 'US Counties', 'All']; // 'San Diego Zip-Codes',
  selectedLocale = this.localesAvailable[0];

  selectedLocations = this.selectedLocationsCountries;

  metricsCountries = [
    'Confirmed Cumulative Cases per 100k',
    'Confirmed Daily Cases per 100k',
    'Confirmed Cumulative Cases',
    'Confirmed Daily Cases',
    'Confirmed Fatality Rate',
    'Time Adjusted Confirmed Fatality Rate',
    'Spread Rate',
    'Daily Growth Rate',
    'Estimated Infection Rate',
  ];

  metricsStates = [
    'Confirmed Cumulative Cases per 100k',
    'Confirmed Daily Cases per 100k',
    'Confirmed Cumulative Cases',
    'Confirmed Daily Cases',
    'Confirmed Fatality Rate',
    'Time Adjusted Confirmed Fatality Rate',
    'Spread Rate',
    'Daily Growth Rate',
    'Estimated Infection Rate'
  ];

  metricsCounties = [
    'Confirmed Cumulative Cases per 100k',
    'Confirmed Daily Cases per 100k',
    'Confirmed Cumulative Cases',
    'Confirmed Daily Cases',
    'Confirmed Fatality Rate',
    'Time Adjusted Confirmed Fatality Rate',
    'Spread Rate',
    'Daily Growth Rate',
  ];

  metricsSDZipCodes = [
    'Confirmed Cumulative Cases per 100k',
    'Confirmed Daily Cases per 100k',
    'Confirmed Cumulative Cases',
    'Confirmed Daily Cases',
    'Confirmed Fatality Rate',
    'Time Adjusted Confirmed Fatality Rate',
    'Spread Rate',
    'Daily Growth Rate',
  ];

  initialMetricIndex = Math.floor(Math.random() * 6);
  selectedMetric = this.metricsCountries[this.initialMetricIndex];
  availableMetrics = this.metricsCountries;

  scaleOptions = ['linear', 'log'];

  alertOptionsCountries = ['Daily High', 'Daily Low', 'Highest biweekly % Uptrend',
    'Highest biweekly % Downtrend', 'Moving Average Crossover Uptrend'];

  alertOptionsStates = ['Daily High', 'Daily Low', 'Highest biweekly % Uptrend',
    'Highest biweekly % Downtrend', 'Moving Average Crossover Uptrend'];

  alertOptionsCounties = ['Highest biweekly % Uptrend',
    'Highest biweekly % Downtrend', 'Moving Average Crossover Uptrend'];

  alertOptionsAll = ['available only for same geo-level'];

  chartDescriptions = [
    'Rate of growth of cumulative confirmed cases. Similar to rho.',
    'Ratio of daily confirmed cases over successive days.',
    'Percentage deaths within confirmed cases. Reported Flu fatality rate in the US is 0.001.',
    'Percentage deaths today within confirmed cases a week ago. Reported Flu fatality rate in the US is 0.001.',
    'Total confirmed cases per 100k in population.',
    'Daily confirmed cases per 100k in population. Value less than 0.5 for 21 days implies the epidemic is under control.'
  ];
  chartDescriptionText = this.chartDescriptions[this.initialMetricIndex];
  noChartData = true;

  selectedScale = this.scaleOptions[0];

  alertPlaceholder = 'Available Presets';
  selectedAlert = this.alertPlaceholder;
  availableAlerts = this.alertOptionsCountries;


  /**
   * Constructor
   * @param service
   */
  constructor(private service: CovidReportService) {
    this.createMultiselectLabels();
  }

  resetDefaults() {
    console.log('resetting to defaults');
    this.selectedLocale = this.localesAvailable[0];
    this.selectedLocations = this.selectedLocationsCountries;
    this.selectedWindow = this.windowsAvailable[1];
    this.selectedScale = this.scaleOptions[0];
    this.selectedMetric = this.metricsCountries[0];
    this.selectedAlert = this.alertPlaceholder;
    this.hidePanel();
  }

  resetAlertDropdown() {
    this.selectedAlert = this.alertPlaceholder;
  }

  /**
   * Update selected window to view, redraw graph
   * @param day - window to get the moving average for
   */
  selectLocale(locale: string) {
    this.selectedLocale = locale;
    this.resetAlertDropdown();
    this.selectedLocations = [];
    if (locale === 'Countries') {
      this.availableAlerts = this.alertOptionsCountries;
      this.availableMetrics = this.metricsCountries;
      this.updateMultiselectLabels(this.locationsCountries);
    }
    if (locale === 'US Counties') {
      this.availableAlerts = this.alertOptionsCounties;
      this.availableMetrics = this.metricsCounties;
      this.updateMultiselectLabels(this.locationsCounties);
    }
    if (locale === 'US States') {
      this.availableAlerts = this.alertOptionsStates;
      this.availableMetrics = this.metricsStates;
      this.updateMultiselectLabels(this.locationsStates);
    }
    if (locale === 'All') {
      this.availableAlerts = this.availableAlertsEmpty;
      this.availableMetrics = this.metricsCounties; // use the smallest set
      this.updateMultiselectLabels(this.locations);
    }
    if (locale === 'San Diego Zip-Codes') {
      this.availableAlerts = this.availableAlertsEmpty;
      this.availableMetrics = this.metricsSDZipCodes;
      this.updateMultiselectLabels(this.locationsZipCodesSanDiego);
    }

    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
  }

  /**
   * Update selected window to view, redraw graph
   * @param day - window to get the moving average for
   */
  selectScale(scale: string) {
    this.selectedScale = scale;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
  }

  /**
  * Update selected window to view, redraw graph
  * @param day - window to get the moving average for
  */
  selectWindow(window: string) {
    this.selectedWindow = window;
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
  }

  /**
   * Update selected metric, redraw graph
   * @param metric - metric type
   */
  selectMetric(metric: string, idx: number) {
    this.selectedMetric = metric;
    if (this.selectedLocale === this.localesAvailable[0]) {
      if (metric === 'Spread Rate' || metric === 'Daily Growth Rate' || metric === 'Confirmed Fatality rate' ||
          metric === 'Time Adjusted Confirmed Fatality Rate') {
        this.selectedScale = this.scaleOptions[0];
      }
    }
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
    this.chartDescriptionText = this.chartDescriptions[idx];
  }


  /**
   *
   */
  setAlertLocaleFromLocale(locale: string) {
    if (locale === this.localesAvailable[0]) {
      return 'country';
    }
    if (locale === this.localesAvailable[1]) {
      return 'state';
    }
    if (locale === this.localesAvailable[2]) {
      return 'county';
    }
    if (locale === this.localesAvailable[3]) {
      return 'sandiegozipcode';
    }
    return 'country';
  }

  /**
   *
   */
  selectAlert(alert: string) {

    const localeAlert = this.setAlertLocaleFromLocale(this.selectedLocale);
    const requestArray = [];

    this.selectedAlert = alert;
    this.selectedMetric = this.availableMetrics[1];

    // if (alert === this.availableAlerts[0]) {
    //   this.selectedLocations = [];
    //   this.selectLocale(this.selectedLocale);
    // } else {
    if (alert === 'Daily High') {
      requestArray.push(this.service.getAlerts(localeAlert, 'DailyHigh'));
    }
    if (alert === 'Daily Low') {
      requestArray.push(this.service.getAlerts(localeAlert, 'DailyLow'));
    }
    if (alert === 'Highest biweekly % Uptrend') {
      requestArray.push(this.service.getAlerts(localeAlert, 'BiweeklyPercentChangeUptrend'));
    }
    if (alert === 'Highest biweekly % Downtrend') {
      requestArray.push(this.service.getAlerts(localeAlert, 'BiweeklyPercentChangeDowntrend'));
    }
    if (alert === 'Moving Average Crossover Uptrend') {
      requestArray.push(this.service.getAlerts(localeAlert, 'MACrossoverUptrend'));
    }
    forkJoin(requestArray)
      .subscribe(allResponses => {
        // Loop over all responses (one per selected location)
        for (let i = 0; i < allResponses.length; i++) {
          const res: any = allResponses[i];
          this.chartDescriptionText = res.description;
          this.selectedLocations = res.locales;
          this.availableLocations = [];
          this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
        }
      });
    // }

  }

  /**
   * Populate the data set to be used for the chart, and then call the chart generation function
   * @param locations - string array of locations to chart
   * @param window - window to view the moving average
   * @param metric - metric type to chart
   */
  getData(locations: Array<string>, window: string, metric: string, scale: String) {
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
              let metricData = entry.metrics[metric];
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
        this.generateChart(displayData, labels, scale);

      }, err => {
        // TODO
      });
  }

  /**
   * Generate the line chart with given input data set.
   * @param data - input data set
   * @param labels - labels for data set
   */
  generateChart(data, labels, scale) {
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

    if (scale === 'linear') {
      this.chart.yScale(anychart.scales.linear());
    }

    if (scale === 'log') {
      this.chart.yScale(anychart.scales.log());
    }


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
   * Set the multi-select options based on the locale list.
   */
  updateMultiselectLabels(localeArray: Array<string>) {
    const locationsMultiselect = [];
    const defaultSelected = [];
    localeArray.forEach(locale => {
      locationsMultiselect.push({ label: locale, value: locale });
    });
    this.availableLocations = locationsMultiselect;
  }

  /**
   * Set the multi-select options based on the locale list.
   */
  createMultiselectLabels() {
    const locationsMultiselect = [];
    const defaultSelected = [];
    this.locationsCountries.forEach(locale => {
      locationsMultiselect.push({ label: locale, value: locale });
    });
    this.availableLocations = locationsMultiselect;
  }

  /**
   * Hide the multiselect panel, kick off chart update.
   */
  hidePanel() {
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
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
    this.getData(this.selectedLocations, this.selectedWindow, this.selectedMetric, this.selectedScale);
  }

}
