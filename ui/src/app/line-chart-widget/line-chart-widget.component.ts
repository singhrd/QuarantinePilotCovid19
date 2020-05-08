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

  // locations = LocationsByCountryName;
  // selectedLocation = 'US';
  locations = ['San Diego', 'New York'];
  selectedLocation = this.locations[0];
  numDaysAvailable = [1, 7, 14, 21];
  selectedNumDays = this.numDaysAvailable[1];

  constructor(private service: CovidReportService) { }

  selectLocation(loc: string) {
    console.log('new location:', loc);
    this.selectedLocation = loc;
    this.getData([this.selectedLocation], this.selectedNumDays);
  }

  selectNumDays(day: number) {
    console.log('new num days:', day);
    this.selectedNumDays = day;
    this.getData([this.selectedLocation], this.selectedNumDays);
  }

  getData(locations: Array<string>, numDays: number) {
    console.log('getData', locations, numDays);
    const displayData = [];

    this.service.getAnnotations('San Diego').subscribe((res: any) => {
      if (res.elements) {
        locations.forEach(location => {
          // Filter based on location
          const filteredData = res.elements.filter(item => item.province_state === location);
          console.log(filteredData);

          console.log(displayData.length);
          // First time through
          if (displayData.length === 0) {
            // TODO will have to do for first entry, then insert for next entries?
            filteredData.forEach(entry => {
              entry.movingAverageGrowthRate.filter(dayInfo => {
                if (dayInfo[0] === numDays) {
                  displayData.push([entry.date, dayInfo[1]]);
                }
              });
            });
          } else {
            // have to do something else for repeat items...
            // displayData.join
          }
        });
        console.log(locations, displayData);
        this.generateChart(displayData);
      }
    });
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
    this.getData(['San Diego'], 7);
    // this.renderer.setProperty(this.el.nativeElement, 'id', this.uuid);
  }

  ngOnChanges(): void {
    console.log('on changes line');
    // this.destroyChart();
    this.getData(['San Diego'], 7);
  }

  ngOnDestroy(): void {
    this.alive = false;
  }

}
