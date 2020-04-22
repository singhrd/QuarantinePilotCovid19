import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { BarChartWidgetComponent } from './bar-chart-widget/bar-chart-widget.component';
import { StackedBarChartComponent } from './charts/stacked-bar-chart/stacked-bar-chart.component';
import { CovidReportService } from './services/covid-report.service';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    BarChartWidgetComponent,
    StackedBarChartComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [CovidReportService],
  bootstrap: [AppComponent]
})
export class AppModule { }
