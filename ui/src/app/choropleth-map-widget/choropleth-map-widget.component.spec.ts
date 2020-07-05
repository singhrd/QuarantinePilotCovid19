import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChoroplethMapWidgetComponent } from './choropleth-map-widget.component';

describe('ChoroplethMapWidgetComponent', () => {
  let component: ChoroplethMapWidgetComponent;
  let fixture: ComponentFixture<ChoroplethMapWidgetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ChoroplethMapWidgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChoroplethMapWidgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
