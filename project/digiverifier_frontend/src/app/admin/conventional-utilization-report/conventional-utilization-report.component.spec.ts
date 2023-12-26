import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConventionalUtilizationReportComponent } from './conventional-utilization-report.component';

describe('ConventionalUtilizationReportComponent', () => {
  let component: ConventionalUtilizationReportComponent;
  let fixture: ComponentFixture<ConventionalUtilizationReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConventionalUtilizationReportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConventionalUtilizationReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
