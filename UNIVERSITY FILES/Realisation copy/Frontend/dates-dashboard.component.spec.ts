import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SdxInputCustomEvent } from '@swisscom/sdx';
import { of } from 'rxjs';
import { FetchingService } from './../../services/api/fetch/fetching.service';
import { DatesDashboardComponent } from './dates-dashboard.component';
import { provideRedux } from '@reduxjs/angular-redux';
import store from 'src/app/store';
import { CommunityCalendarTravelRequestDto } from 'src/app/common/types/CommunityCalendarTravelRequestDto';

describe('DatesDashboardComponent', () => {
  let component: DatesDashboardComponent;
  let fixture: ComponentFixture<DatesDashboardComponent>;

  interface ContingentService {
    fetchAdminContingentData: jasmine.Spy;
  }

  interface UserService {
    fetchUserData: jasmine.Spy;
  }

  const mockFetchingService = {
    contingent: {
      fetchAdminContingentData: jasmine.createSpy().and.returnValue(of([])),
    } as ContingentService,
    user: {
      fetchUserData: jasmine.createSpy().and.returnValue(of({ id: 1 })),
    } as UserService,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DatesDashboardComponent],
      imports: [RouterTestingModule, TranslateModule.forRoot()],
      providers: [{ provide: FetchingService, useValue: mockFetchingService }, provideRedux({ store }),],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(DatesDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should mark current month correctly', () => {
    const month = new Date().getMonth();
    expect(component.isCurrentMonth(month)).toBe('current');
    expect(component.isCurrentMonth((month + 1) % 12)).toBe('');
  });

  it('should select and deselect productive dates', () => {
    const month = 0;
    const day = 1;
    component.selectDate(month, day);
    expect(component.selectedProductDates[component.direction].length).toBe(1);

    component.selectDate(month, day);
    expect(component.selectedProductDates[component.direction].length).toBe(0);
  });

  it('should not allow more than 180 productive dates', () => {
    const month = 0;
    component.allowedAmountDatesProductive = 1;
    component.selectDate(month, 0); // First one
    component.selectDate(month, 1); // Second one, should be blocked

    expect(component.selectedProductDates[component.direction].length).toBe(1);
  });

  it('should switch travel type to productive', () => {
    component.selectTravelType(true);
    expect(component.isProductiveSelected).toBeTrue();
  });

  it('should update location and state on changeOption', () => {
    const event = { target: { value: 5 } } as unknown as SdxInputCustomEvent<unknown>; // NL to CH
    component.changeOption(event);
    expect(component.direction).toBe(5);
    expect(component.location).toBe('Latvia');
    expect(component.isBaseLocationCH).toBeFalse();
  });

  it('should return true if date is productive', () => {
    const date = new Date(component.year, 0, 5);
    component.selectedProductDates[component.direction].push(date);
    expect(component.isProductiveDate(0, 4)).toBeTrue(); // dayIndex is 4 → 5th
  });

  it('should return true if date is blocked', () => {
    expect(component.isBlockedDate(0, 0)).toBeTrue();
  });

  it('should enable edit mode and save current state', () => {
    component.selectedProductDates[component.direction].push(new Date(2025, 0, 1));
    component.enableEditMode();
    expect(component.isEditable).toBeTrue();
    expect(component.stateBeforeEditProductDates[component.direction].length).toBe(1);
  });
  
  it('should disable edit mode and revert changes if noRevert is false or missing', () => {
    const originalDate = new Date(2025, 0, 1);
    component.selectedProductDates[component.direction] = [originalDate];
    component.enableEditMode();
  
    // Simulate change
    component.selectedProductDates[component.direction] = [];
  
    component.disableEditMode();
    expect(component.isEditable).toBeFalse();
    expect(component.selectedProductDates[component.direction]).toEqual([originalDate]);
  });
  
  it('should disable edit mode without reverting if noRevert is true', () => {
    component.enableEditMode();
    component.selectedProductDates[component.direction].push(new Date(2025, 0, 1));
  
    component.disableEditMode(true);
    expect(component.isEditable).toBeFalse();
    expect(component.selectedProductDates[component.direction].length).toBe(1);
  });

  it('should show allocation days when toggled', () => {
    expect(component.isShowAllocationDaysSelected).toBeFalse();
    component.isShowAllocationDaysSelected = true;
    expect(component.isShowAllocationDaysSelected).toBeTrue();
  });

  it('should detect a productive past day without travel as reallocatable', () => {
    const today = new Date();
    const pastDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 3);
    const month = pastDate.getMonth();
    const dayIndex = pastDate.getDate() - 1;
  
    component.selectedProductDates[component.direction].push(pastDate);
    component.isTraveled[month][dayIndex] = false;
  
    const result = component.shouldDateBeReallocated(month, dayIndex);
    expect(result).toBeTrue();
  });
  
  it('should not reallocate if the date is not productive', () => {
    const today = new Date();
    const pastDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 3);
    const month = pastDate.getMonth();
    const dayIndex = pastDate.getDate() - 1;
  
    // Ensure it's not in selectedProductDates
    component.isTraveled[month][dayIndex] = false;
  
    const result = component.shouldDateBeReallocated(month, dayIndex);
    expect(result).toBeFalse();
  });
  
  it('should not reallocate if the date was traveled', () => {
    const pastDate = new Date(2025, 0, 5); // Jan 5
    const month = pastDate.getMonth();
    const dayIndex = pastDate.getDate() - 1;
  
    component.selectedProductDates[component.direction].push(pastDate);
    component.isTraveled[month][dayIndex] = true;
  
    const result = component.shouldDateBeReallocated(month, dayIndex);
    expect(result).toBeFalse();
  });

  it('should return true if the date is in the past', () => {
    const pastMonth = new Date().getMonth();
    const pastDay = new Date().getDate() - 3;
    expect(component.isDateInPast(pastMonth, pastDay)).toBeTrue();
  });
  
  it('should return false if the date is today or in the future', () => {
    const today = new Date();
    expect(component.isDateInPast(today.getMonth(), today.getDate())).toBeFalse();
  });

  it('should select a date when searchTravelRequests is called', () => {
    component.searchTravelRequests(0, 0);
    expect(component.selectedDate).toEqual(new Date(component.year, 0, 1));
  });
  
  it('should return empty travelers if no travelRequests', () => {
    component.selectedDate = new Date(component.year, 0, 10);
    component.travelRequests = [];
    const travelers = component.getTravelers();
    expect(travelers).toEqual([]);
  });

  it('should return travelers whose travel includes selectedDate', () => {
    component.selectedDate = new Date(component.year, 0, 10);
  
    component.travelRequests = [
      {
        departureDate: new Date(component.year, 0, 8).toISOString(),
        returnDate: new Date(component.year, 0, 12).toISOString(),
        user: {
          firstName: 'Alice',
          lastName: 'Smith',
          departmentName: 'R&D',
        },
        id: 'abc123',
        country: 'LV',
      } as unknown as CommunityCalendarTravelRequestDto,
    ];
  
    const travelers = component.getTravelers();
    expect(travelers.length).toBe(1);
    expect(travelers[0].name).toBe('Alice Smith');
    expect(travelers[0].department).toBe('R&D');
    expect(travelers[0].id).toBe('abc123');
  });

  it('should return empty array if selectedDate is null', () => {
    component.selectedDate = null;
  
    component.travelRequests = [
      {
        departureDate: new Date(component.year, 0, 1).toISOString(),
        returnDate: new Date(component.year, 0, 5).toISOString(),
        user: { firstName: 'John', lastName: 'Doe', departmentName: 'IT' },
        id: '123',
        country: 'LV',
      } as unknown as CommunityCalendarTravelRequestDto,
    ];
  
    const travelers = component.getTravelers();
    expect(travelers).toEqual([]);
  });

  it('should return true for a traveled date', () => {
    const req = {
      departureDate: new Date(component.year, 0, 5).toISOString(),
      returnDate: new Date(component.year, 0, 5).toISOString(),
      user: { firstName: 'Test', lastName: 'User', departmentName: 'IT' },
      id: '1',
      country: 'LV'
    } as unknown as CommunityCalendarTravelRequestDto;
  
    component.travelRequests = [req];
    const isTraveled = component.isTraveledOnDay(0, 4); // Jan 5
    expect(isTraveled).toBeTrue();
  });
});
