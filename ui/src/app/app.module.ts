import { BrowserModule } from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { MultiSelectModule } from 'primeng/multiselect';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { BarChartWidgetComponent } from './bar-chart-widget/bar-chart-widget.component';
import { StackedBarChartComponent } from './charts/stacked-bar-chart/stacked-bar-chart.component';
import { CovidReportService } from './services/covid-report.service';
import { LineChartWidgetComponent } from './line-chart-widget/line-chart-widget.component';
import { LineChartComponent } from './charts/line-chart/line-chart.component';


@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    BarChartWidgetComponent,
    StackedBarChartComponent,
    LineChartWidgetComponent,
    LineChartComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    MultiSelectModule
  ],
  providers: [CovidReportService],
  bootstrap: [AppComponent]
})
export class AppModule { }
