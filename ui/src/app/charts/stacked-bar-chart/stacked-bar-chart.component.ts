import { Component, OnInit, OnDestroy, OnChanges, ViewChild, ViewEncapsulation, Input, Renderer2, ElementRef } from '@angular/core';
import { ResultsPerDate } from '../../models/data-types';
import 'anychart';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class StackedBarChartComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('chartContainer') chartContainer;

  @Input() uuid: string;
  @Input() inputDataSet: any[];
  @Input() inputDataLabels: string[];
  @Input() ylabel: string;

  // Widget vars
  alive = false;

  // Data set vars
  data: Array<ResultsPerDate> = [];
  dataSet: anychart.data.Set = null;

  // Chart vars
  chart: anychart.charts.Cartesian = null;

  /**
   * Component constructor.
   */
  constructor(private renderer: Renderer2, private el: ElementRef) { }

  /**
   * Generate chart element.
   */
  generateChart(): void {

    // Create a data set from input data
    this.dataSet = anychart.data.set(this.inputDataSet);

    anychart.graphics.useAbsoluteReferences(false);

    // Create chart object
    this.chart = anychart.column();
    this.chart.animation(true);
    this.chart.yScale().stackMode('value');

    // Create series data
    for (let idx = this.inputDataLabels.length - 1; idx > 0; idx--) {
      // Map data for each series, take x from the zero column and value from the column of data set
      this.chart.column(
        this.dataSet.mapAs({ x: 0, value: idx, label: this.inputDataLabels[idx] }))
        .name(this.inputDataLabels[idx]
        );
    }

    // Turn on legend
    this.chart.legend().enabled(true).fontSize(13).padding([0, 0, 20, 0]).inverted(true);
    // Set yAxis labels formatter
    this.chart.yAxis().labels().format('{%Value}{groupsSeparator: }');

    // Set titles for axes
    this.chart.yAxis().title(this.ylabel);

    // Set interactivity hover
    this.chart.interactivity().hoverMode('by-x');

    this.chart.tooltip().displayMode('union');
    this.chart.tooltip().useHtml(true);
    this.chart.tooltip().unionFormat(function() {
      let total = 0;
      let tip = '';
      this.points.forEach(element => {
        tip += element.seriesName + ': ' + element.value.toLocaleString() + '<br>';
        total += +element.value;
      });
      return tip + '<strong>Total: ' + total.toLocaleString() + '</strong>';
    });

    // Set container id for the chart
    this.chart.container('chartContainer');

    // Initiate chart drawing
    this.chart.draw();
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

  ngOnInit(): void {
    this.alive = true;
    this.renderer.setProperty(this.el.nativeElement, 'id', this.uuid);
  }

  ngOnChanges(): void {
    this.destroyChart();
    this.generateChart();
  }

  ngOnDestroy(): void {
    this.alive = false;
  }

}
