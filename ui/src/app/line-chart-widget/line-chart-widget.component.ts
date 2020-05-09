import { Component, OnInit, OnChanges, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { LocationsByCountryName } from '../models/data-types';
import { CovidReportService } from '../services/covid-report.service';

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
  dataSet2: anychart.data.Set = null;

  locations = LocationsByCountryName;
  // selectedLocation = 'US';
  // locations = ['San Diego', 'New York'];
  selectedLocation = this.locations[0];
  numDaysAvailable = [1, 7, 14, 21];
  selectedNumDays = 7;
  metrics=['alpha','growth','death'];
  selectedMetric = this.metrics[0];

  constructor(private service: CovidReportService) { }

  selectLocation(loc: string) {
    console.log('new location:', loc);
    this.selectedLocation = loc;
    this.getData([this.selectedLocation], this.selectedNumDays, this.selectedMetric);
  }

  selectNumDays(day: number) {
    console.log('new num days:', day);
    this.selectedNumDays = day;
    this.getData([this.selectedLocation], this.selectedNumDays, this.selectedMetric);
  }

  selectMetric(metric: string) {
    console.log('new metric:', metric);
    this.selectedMetric = metric;
    this.getData([this.selectedLocation], this.selectedNumDays, this.selectedMetric);
  }
    getData(locations: Array<string>, numDays: number, metric: string) {
    console.log('getData', locations, numDays, metric);
    const displayData = [];

    this.service.getAnnotations(this.selectedLocation).subscribe((res: any) => {
      if (res.elements) {
          // Filter based on location
          console.log(res.elements);
          console.log(displayData.length);
          // First time through
          if (displayData.length === 0) {
            // TODO will have to do for first entry, then insert for next entries?
            res.elements.forEach(entry => {
              let metricData = entry.movingAverageGrowthRate;
              if(metric === 'alpha') {
                metricData = entry.movingAverageEstimatedAlpha;
              }
              if(metric === 'death'){
                metricData = entry.movingAverageDeathRate;
              }
              metricData.filter(dayInfo => {
                if (dayInfo[0] === numDays) {
                  displayData.push([entry.date, dayInfo[1]]);
                }
            });
            });
          } else {
            // have to do something else for repeat items...
            // displayData.join
          }
        console.log(locations, displayData);
        this.generateChart(displayData);
      }
    });


    // console.log(displayData);

    // return displayData;

    // return [
    //   ['1986', 3.6, 2.3, 2.8, 11.5],
    //   ['1987', 7.1, 4.0, 4.1, 14.1],
    //   ['1988', 8.5, 6.2, 5.1, 17.5],
    //   ['1989', 9.2, 11.8, 6.5, 18.9],
    //   ['1990', 10.1, 13.0, 12.5, 20.8],
    //   ['1991', 11.6, 13.9, 18.0, 22.9],
    //   ['1992', 16.4, 18.0, 21.0, 25.2],
    //   ['1993', 18.0, 23.3, 20.3, 27.0],
    //   ['1994', 13.2, 24.7, 19.2, 26.5],
    //   ['1995', 12.0, 18.0, 14.4, 25.3],
    //   ['1996', 3.2, 15.1, 9.2, 23.4],
    //   ['1997', 4.1, 11.3, 5.9, 19.5],
    //   ['1998', 6.3, 14.2, 5.2, 17.8],
    //   ['1999', 9.4, 13.7, 4.7, 16.2],
    //   ['2000', 11.5, 9.9, 4.2, 15.4],
    //   ['2001', 13.5, 12.1, 1.2, 14.0],
    //   ['2002', 14.8, 13.5, 5.4, 12.5],
    //   ['2003', 16.6, 15.1, 6.3, 10.8],
    //   ['2004', 18.1, 17.9, 8.9, 8.9],
    //   ['2005', 17.0, 18.9, 10.1, 8.0],
    //   ['2006', 16.6, 20.3, 11.5, 6.2],
    //   ['2007', 14.1, 20.7, 12.2, 5.1],
    //   ['2008', 15.7, 21.6, 10, 3.7],
    //   ['2009', 12.0, 22.5, 8.9, 1.5]
    // ];
  }

  generateChart(data) {
    this.destroyChart();
    console.log('destroy chart line');
    this.chart = anychart.line();

    console.log('received data', data);
    // get data
    // create data set on our data
    let dataSet = anychart.data.set(data);
    console.log(dataSet);

    // map data for the first series, take x from the zero column and value from the first column of data set
    let seriesData_1 = dataSet.mapAs({ 'x': 0, 'value': 1 });

    // // map data for the second series, take x from the zero column and value from the second column of data set
    // let seriesData_2 = dataSet.mapAs({ 'x': 0, 'value': 2 });

    // // map data for the third series, take x from the zero column and value from the third column of data set
    // let seriesData_3 = dataSet.mapAs({ 'x': 0, 'value': 3 });

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
    this.chart.yAxis().title('Moving Average Growth Rate');
    this.chart.xAxis().labels().padding(5);

    // create first series with mapped data
    let series_1 = this.chart.line(seriesData_1);
    series_1.name(this.selectedLocation);
    series_1.hovered().markers()
      .enabled(true)
      .type('circle')
      .size(4);
    series_1.tooltip()
      .position('right')
      .anchor('left-center')
      .offsetX(5)
      .offsetY(5);

    // // create second series with mapped data
    // let series_2 = this.chart.line(seriesData_2);
    // series_2.name('Whiskey');
    // series_2.hovered().markers()
    //   .enabled(true)
    //   .type('circle')
    //   .size(4);
    // series_2.tooltip()
    //   .position('right')
    //   .anchor('left-center')
    //   .offsetX(5)
    //   .offsetY(5);

    // // create third series with mapped data
    // let series_3 = this.chart.line(seriesData_3);
    // series_3.name('Tequila');
    // series_3.hovered().markers()
    //   .enabled(true)
    //   .type('circle')
    //   .size(4);
    // series_3.tooltip()
    //   .position('right')
    //   .anchor('left-center')
    //   .offsetX(5)
    //   .offsetY(5);

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
    this.getData(['San Diego'], 7, 'growth');
    // this.renderer.setProperty(this.el.nativeElement, 'id', this.uuid);
  }

  ngOnChanges(): void {
    console.log('on changes line');
    // this.destroyChart();
    this.getData(['San Diego'], 7,'growth');
  }

  ngOnDestroy(): void {
    this.alive = false;
  }

}
