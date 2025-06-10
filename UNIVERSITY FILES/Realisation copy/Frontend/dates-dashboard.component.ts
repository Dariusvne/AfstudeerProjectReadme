import { FormStyle, getLocaleMonthNames, TranslationWidth } from '@angular/common';
import { Component, effect, Signal, AfterViewInit, OnInit } from '@angular/core';
import { Directions, TravelRuleSettingsType } from 'src/app/common/types/TravelRuleSettings';
import { UserDetailType } from 'src/app/common/types/UserDetail';
import { UserGroupType } from 'src/app/common/types/Usergroup';
import { FetchingService } from './../../services/api/fetch/fetching.service';
import dayjs from 'dayjs';
import { SdxInputCustomEvent, SdxTabsCustomEvent } from '@swisscom/sdx';
import { ContingentsApiService } from 'src/app/services/endpoints/contingents-api.service';
import { CommunityCalendarTravelRequestDto } from 'src/app/common/types/CommunityCalendarTravelRequestDto';



@Component({
  selector: 'app-dates-dashboard',
  template: `
    <div>
      <div id="header" class="notification__content"></div>
      <app-banner>
        <div class="white d-flex flex-column justify-content-center">
          <h1 class="d2 m-0 p-0 text-align-center white">Contingents</h1>
        </div>
      </app-banner>

      <div class="container d-flex flex-column gap-4 pb-4">
        <div>
          <div>
            <sdx-tabs sr-hint="Tabs with text." theme="centered" (sdxselect)="changeOption($event)">
              <sdx-tabs-item [value]="0" label="CH to LV" data-cy="tab-CH_LV"></sdx-tabs-item>
              <sdx-tabs-item [value]="1" label="CH to NL" data-cy="tab-CH_NL"></sdx-tabs-item>
              <sdx-tabs-item [value]="2" label="LV to CH" data-cy="tab-LV_CH"></sdx-tabs-item>
              <sdx-tabs-item [value]="3" label="LV to NL" data-cy="tab-LV_NL"></sdx-tabs-item>
              <sdx-tabs-item [value]="4" label="NL to LV" data-cy="tab-NL_LV"></sdx-tabs-item>
              <sdx-tabs-item [value]="5" label="NL to CH" data-cy="tab-NL_CH"></sdx-tabs-item>
            </sdx-tabs>
          </div>
          <div class="d-flex flex-row gap-4 justify-content-between flex-wrap">
            <div class="d-flex flex-row">
              <div class="d-flex flex-column gap-2">
                <div class="d-flex flex-row  w-100" style="flex:1">
                  <div class="d-column">

                      <h2 class="h2 m-0">{{ year }}</h2>
                      <div *ngIf="isShowAllocationDaysSelected" class="d-flex my-2">
                        <div class="color-box reallocate"></div>
                        <div
                        class="label productive mb-0 my-auto text-standard"
                        [ngClass]="{ selected: isProductiveSelected }">
                        Days eligible for reallocation
                      </div>
                    </div>
                  </div>
                </div>
                <div class="calendar-container">
                  <div class="month-container">
                    <div
                      *ngFor="let month of months; let monthIndex = index"
                      class="card month-card"
                      [ngClass]="isCurrentMonth(monthIndex)"
                    >
                      <div class="h4">{{ month }}</div>
                      <div *ngIf="!isShowAllocationDaysSelected" class="calendar-weekdays">
                        <div class="day-name">{{ 'mon' | translate }}</div>
                        <div class="day-name">{{ 'tue' | translate }}</div>
                        <div class="day-name">{{ 'wed' | translate }}</div>
                        <div class="day-name">{{ 'thu' | translate }}</div>
                        <div class="day-name">{{ 'fri' | translate }}</div>
                        <div class="day-name">{{ 'sat' | translate }}</div>
                        <div class="day-name">{{ 'sun' | translate }}</div>

                        <div *ngFor="let dayIndex of Arr(firstMonthDay[monthIndex])" class="empty"></div>
                        <div
                          *ngFor="let dayIndex = index; of: Arr(daysInMonths[monthIndex])"
                          class="day"
                          tabindex="0"
                          role="button"
                          [attr.data-cy]="'day-'+monthIndex + '-' +dayIndex"                         
                          [attr.aria-label]="'Select day ' + (dayIndex + 1) + ' of ' + months[monthIndex]"
                          [ngClass]="{
                        'productive-hover': isProductiveSelected === true && isEditable,
                        'not-allowed': isBlockedDate(monthIndex, dayIndex),
                        allowed: isProductiveDate(monthIndex, dayIndex),
                        'allowed-non-prod': isUnproductiveDate(monthIndex, dayIndex)
                      }"
                          (click)="isEditable ? selectDate(monthIndex, dayIndex) : searchTravelRequests(monthIndex, dayIndex)"
                          (keydown.enter)="selectDate(monthIndex, dayIndex)"
                          (keydown.space)="selectDate(monthIndex, dayIndex); $event.preventDefault()"
                        >
                          {{ dayIndex + 1 }}
                        </div>
                      </div>
                      <div *ngIf="isShowAllocationDaysSelected" class="calendar-weekdays">
                        <div class="day-name">{{ 'mon' | translate }}</div>
                        <div class="day-name">{{ 'tue' | translate }}</div>
                        <div class="day-name">{{ 'wed' | translate }}</div>
                        <div class="day-name">{{ 'thu' | translate }}</div>
                        <div class="day-name">{{ 'fri' | translate }}</div>
                        <div class="day-name">{{ 'sat' | translate }}</div>
                        <div class="day-name">{{ 'sun' | translate }}</div>

                        <div *ngFor="let dayIndex of Arr(firstMonthDay[monthIndex])" class="empty"></div>
                        <div
                          *ngFor="let dayIndex = index; of: Arr(daysInMonths[monthIndex])"
                          class="day"
                          tabindex="0"
                          role="button"
                          [attr.data-cy]="'day-'+monthIndex + '-' +dayIndex"                         
                          [attr.aria-label]="'Select day ' + (dayIndex + 1) + ' of ' + months[monthIndex]"
                          [ngClass]="{
                        'not-allowed': !shouldDateBeReallocated(monthIndex, dayIndex),
                        reallocation: shouldDateBeReallocated(monthIndex, dayIndex),
                      }"
                          (click)="shouldDateBeReallocated(monthIndex, dayIndex) ? selectDate(monthIndex, dayIndex) : null"
                          (keydown.enter)="selectDate(monthIndex, dayIndex)"
                          (keydown.space)="selectDate(monthIndex, dayIndex); $event.preventDefault()"
                        >
                          {{ dayIndex + 1 }}
                        </div>
                        
               
                      </div>

                    </div>
                  </div>
                  <div class="traveler-list-container pr-2" *ngIf="selectedDate">
                      <app-traveler-list
                        [title]="titleSelectedDate()"
                        [travelers]="getTravelers()"
                        [isProduction]="false"
                        destinationName="Netherlands"
                        [emptyStateSubtitle]="shouldDateBeReallocated(selectedDate.getMonth(), selectedDate.getDate() - 1) ? 'You can reallocate this date' : 'No one is traveling to Netherlands on this date'">
                      </app-traveler-list>
                  </div>
                </div>
              </div>
            </div>
            <div
              class="d-flex flex-row justify-content-between w-100"
              [ngClass]="isReachedBottom ? '' : 'bottom-bar'"
            >
              <div class="container d-flex flex-row justify-content-between py-4">
                <div class="d-flex justify-content-start " style="flex:1">
                  <div class="legend gap-2 flex-wrap">
                    <div
                      class="legend-item"
                      (click)="selectTravelType(true)"
                      (keydown.enter)="selectTravelType(true)"
                      (keydown.space)="selectTravelType(true); $event.preventDefault()"
                      tabindex="0"
                      role="button"
                      aria-label="Select productive working days"
                    >
                      <div
                        [ngClass]="{ selected: isProductiveSelected }"
                        class="color-box allowed"
                      ></div>
                      <div
                        class="label productive mb-0 my-auto text-standard"
                        [ngClass]="{ selected: isProductiveSelected }">
                        Working days:
                        {{ this.selectedProductDates[direction].length }}/{{
                          isBaseLocationCH ? 180 : allowedAmountDatesProductive
                        }}
                      </div>
                    </div>
                    <!-- TODO: current logic we don't have non-productive date -->
                    <!-- <div
                      *ngIf="!isBaseLocationCH"
                      class="legend-item"
                      (click)="selectTravelType(false)"
                      (keydown.enter)="selectTravelType(false)"
                      (keydown.space)="selectTravelType(false); $event.preventDefault()"
                      tabindex="0"
                      role="button"
                      aria-label="Select non-productive days"
                    >
                      <div
                        [ngClass]="{ selected: !isProductiveSelected }"
                        class="color-box allowed-non-prod"
                      ></div>
                      <div
                        class="label unproductive mb-0 my-auto  text-standard"
                        [ngClass]="{ selected: !isProductiveSelected }"
                      >
                        {{ 'allowedTravelNonProductive' | translate }}:
                    <div class="legend-item" tabindex="0">
                      <div class="color-box not-allowed  text-standard"></div>
                      <div class="label mb-0  my-auto">
                      </div>
                    </div> -->
                    <div class="legend-item">
                      <div class="color-box not-allowed  text-standard"></div>
                      <div class="label mb-0  my-auto">
                        {{ 'wdAndNwd' | translate }}:
                        {{
                          180 -
                            selectedNonProductDates[direction].length -
                            selectedProductDates[direction].length
                        }}/180
                      </div>
                    </div>
                  </div>
                </div>
                <div *ngIf="!isEditable" data-cy="edit-button" class="d-flex justify-content-end my-auto" style="flex:1">
                  <sdx-button
                    [disabled]="isLoading"
                    class="edit-button"
                    (click)="enableEditMode()"
                    label="Edit"
                  ></sdx-button>
                </div>
                <div *ngIf="isEditable" class="d-flex gap-3 justify-content-end my-auto" style="flex:1">
                  <sdx-input-item 
                    data-cy="toggle-allocation-days"
                    class="d-flex align-items-center" 
                    type="checkbox" 
                    [checked]="isShowAllocationDaysSelected"
                    (change)="isShowAllocationDaysSelected = !isShowAllocationDaysSelected">
                      Show allocation days
                  </sdx-input-item>
                  <sdx-button
                      data-cy="cancel-button"
                      [disabled]="isLoading"
                      class="cancel-button"
                      (click)="disableEditMode()"
                      label="Cancel"
                    ></sdx-button>
                    <sdx-button
                      data-cy="submit-button"
                      [disabled]="isLoading"
                      class="submit-button"
                      (click)="submitChanges()"
                      label="Submit"
                    ></sdx-button>
                </div>
              </div>
            </div>
          </div>
          <div style="height:150px" *ngIf="!isReachedBottom"></div>
        </div>
      </div>
    </div>
  `,
  styles: [/* css */`
    h1 {
      padding-top: 3vw;
    }

    .calendar-container {
      display: flex;
      height: 60vh; 
      gap: 2rem;
      overflow-y: hidden;
    }

    .month-container {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      overflow-y: auto; 
      direction: row;
      max-width: 70%; 
    }

    .traveler-list-container {
      height: 60vh;
      overflow-y: auto;
      align-self: flex-start;
    }

    .month-card {
      min-width: 420px;
      max-width:420px;
      margin: 0 !important;
      flex: 1;
    }

    .card::before {
      background: var(--sdx-color-gray-tint-7);
    }

    .card.current::before {
      background: var(--sdx-color-int-green);
    }

    .bottom-bar {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      z-index: 1000;
      padding-bottom: 1rem;
      transition: 0.2s ease-in-out all;
    }

    .bottom-bar .container {
      flex-wrap:wrap;
      min-height:96px;
      background-color: #F8FAFB70;
      backdrop-filter: blur(80px);
      padding: 12px 24px !important;
      border: 1px solid #D6D6D6;
      border-radius: 12px;
    }

    .calendar-weekdays {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 10px;
      cursor: default;
    }

    .day {
      cursor: pointer;
      padding: 0 !important;
    }

    .day.no-travels {
      border: 2px solid red;
      border-radius: 10px; /* optional, makes corners slightly rounded */
      padding: 2px;       /* optional, gives some spacing inside the border */
    } 

    .day-name {
      padding: 0;
    }

    .day, .empty, .day-name {
      text-align: center;
      border-radius: 10px;
      height: 42px;
      width:42px;
      max-width:42px;
      line-height: 42px;
    }

    .day {
      transition: 0.2s ease-in-out background;
    }

    .day.productive-hover:hover {
      color: white;
      background-color: #1C8912B9;
      box-shadow: rgba(0, 0, 0, 0.15) 2.4px 2.4px 3.2px;
      border: none;
    }

    .day:hover {
      color: white;
      background-color: #0A72E9C2;
      box-shadow: rgba(0, 0, 0, 0.15) 2.4px 2.4px 3.2px;
      border: none;
    }

    .day:active {
      transform: scale(0.95);
    }

    .allowed, .allowed:hover {
      background-color: #1C8912;
      color: white;
    }

    .reallocation, .rellocation:hover {
      background-color: rgba(207, 74, 12);
      color: white;
    }

   .reallocate {
      background-color: rgba(207, 74, 12);
      color: white;
    }

    .color-box.allowed.selected {
      border-color: #12610BFF;
      transition: 0.2s ease-in-out all;
    }

    .allowed-non-prod {
      background-color: #0A71E9;
      color: white;
    }

    .color-box.allowed-non-prod.selected  {
      border-color: #0943B0FF;
      transition: 0.2s ease-in-out all;
    }

    .color-box.allowed {
      border: 5px solid #1C8912;
      transition: 0.2s ease-in-out all;
    }
    .color-box.allowed-non-prod {
      border: 5px solid #0A71E9;
      transition: 0.2s ease-in-out all;
    }

    .not-allowed {
      background: #C4C4C4FF;
      color:#8C8C8CFF;
    }

    .legend-item .label{
      white-space: nowrap;
    }

    .legend-item .label.productive.selected {
      color:#1C8912;
      border-bottom: 2px solid #1C8912;
      transition: 0.2s ease-in-out all;
    }

    .legend-item .label.unproductive.selected {
      color: #0A71E9;
      border-bottom: 2px solid #0A71E9;
      transition: 0.2s ease-in-out all;
    }

    .legend-item .label.productive:hover {
      border-bottom: 2px solid #22A816FF;
      transition: 0.2s ease-in-out all;
    }
    .legend-item .label.unproductive:hover  {
      border-bottom: 2px solid #0A71E9;
      transition: 0.2s ease-in-out all;
    }

    .legend-item .label {
      font-weight: 500;
      cursor: pointer;
      border-bottom: 2px solid rgba(255,255,255,0);
    }


    .color-box {
      min-width: 24px;
      min-height: 24px;
      border-radius: 4px;
      margin-right: 8px;
    }

    .legend {
      display: flex;
      flex-direction: row;
      justify-content: left;
      align-items: center;
    }

    .legend-item {
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      margin-right: 20px;
      cursor: pointer;
    }

    .legend-item .label {
      cursor: pointer;
    }

    .bar-item {
      justify-content: center;
      padding: 0;
    }

    .button-box {
      margin: 0 4%;
    }

    .year-picker {
      padding: 2px;
    }

    .middle-bar-item {
      width: 100%;
    }

    .overlay {
      height: 100%;
      width: 100%;
      left:0;
      top:0;
      position: fixed;
      display: flex;
      justify-content: center;
      align-items: center;
      background-color: rgba(255, 255, 255, 0.7);
      z-index: 1000;
    }

  `,
]})
export class DatesDashboardComponent implements OnInit, AfterViewInit {
  workingDays = 0;
  location = '';
  direction: Directions = Directions.CH_LV;
  isEditable = false;

  year = new Date().getFullYear();
  currentYear = new Date().getFullYear();
  readonly months = getLocaleMonthNames('en-US', FormStyle.Standalone, TranslationWidth.Wide);

  daysInMonths: number[] = this.getDaysInMonth();
  firstMonthDay: number[] = this.getFirstDayOfMonth();
  Arr = Array;

  userDetail: Signal<UserDetailType>;
  contingentUsergroup: Signal<UserGroupType[]>;
  datesData: TravelRuleSettingsType[] = [];

  travelRequests: CommunityCalendarTravelRequestDto[];

  travelRequestsLoading = false;
  isTraveled: boolean[][] = this.initializeEmptyBooleanMatrix();


  datesCurrYear: number[][] = [[0]];
  datesNextYear: number[][] = [[0]];

  dates: number[][] = this.datesCurrYear;

  allowedAmountDatesProductive = 180; //180 for now
  allowedAmountDatesNonProductive = 0;
  isProductiveSelected = true;
  isBaseLocationCH = true;

  selectedProductDates: { [key in Directions]: Date[] } = {
    0: [],
    1: [],
    2: [],
    3: [],
    4: [],
    5: [],
  };
  selectedNonProductDates: { [key in Directions]: Date[] } = {
    0: [],
    1: [],
    2: [],
    3: [],
    4: [],
    5: [],
  };

  stateBeforeEditProductDates: { [key in Directions]: Date[] } = {...this.selectedProductDates};
  stateBeforeEditNonProductDates: { [key in Directions]: Date[] } = {...this.selectedNonProductDates};

  isShowAllocationDaysSelected = false;

  selectedDate: Date | null;

  datesProductive: number[][] = [[0]];
  datesNonProductive: number[][] = [[0]];

  yearOptions = [{ name: this.currentYear, value: this.currentYear, checked: 'true' }];

  isReachedBottom = false;
  isLoading = false;

  constructor(
    private fetchingService: FetchingService,
    private contingentsApiService: ContingentsApiService,
  ) {
    this.userDetail = this.fetchingService.user.fetchUserData();
    this.contingentUsergroup = this.fetchingService.contingent.fetchAdminContingentData();
  }

  effectForDatesData = effect(() => {
    this.syncDatesData();
  });

  ngOnInit(): void {
    this.loadTravelRequests(this.direction);
  }

  syncDatesData() {
    this.datesData = [];

    if (this.contingentUsergroup().length === 0) return;

    for (const userGroup of this.contingentUsergroup()) {
      if (userGroup?.travelRuleSettings?.length) {
        for (const travelRule of userGroup.travelRuleSettings) {
          this.datesData.push(travelRule as TravelRuleSettingsType);
        }
      }
    }
    this.syncDates();
  }

  ngAfterViewInit(): void {
    const isAtBottom = (): boolean => {
      return window.innerHeight + window.scrollY + 80 >= document.body.offsetHeight;
    };
    const onReachBottom = (): void => {
      // Your logic here
      this.isReachedBottom = true;
    };
    window.addEventListener('scroll', () => {
      if (isAtBottom()) {
        onReachBottom();
      } else {
        this.isReachedBottom = false;
      }
    });
  }

  searchTravelRequests(monthIndex: number, dayIndex: number){
    this.selectedDate = this.getDate(monthIndex, dayIndex);
  }

  getTravelers() : {
    id: string;
    name: string;
    department: string;
    totalDays: string;
    hasImage: boolean;
  }[]{
    return this.travelRequests.filter((travelRequest: CommunityCalendarTravelRequestDto) => {
      const start = new Date(travelRequest.departureDate);
      const end = new Date(travelRequest.returnDate);

      start.setHours(0, 0, 0, 0);
      end.setHours(0, 0, 0, 0);
  
      // Check if dayDate is in [start, end] (inclusive)
      if (!this.selectedDate)
        return;
      return this.selectedDate >= start && this.selectedDate <= end;
    }).map((travelRequest: CommunityCalendarTravelRequestDto) => {
      return {
        id: travelRequest.id,
        name: travelRequest.user.firstName + ' ' + travelRequest.user.lastName,
        department: travelRequest.user.departmentName,
        totalDays: travelRequest.departureDate + " - " + travelRequest.returnDate,
        hasImage: false
      }
    })
  }

  titleSelectedDate(): string {
    if (!this.selectedDate) return dayjs().format('ddd DD, MMMM');
    return dayjs(this.selectedDate).format('ddd DD, MMMM');
  }

  isCurrentMonth(month: string | number) {
    return new Date().getMonth() === Number(month) ? 'current' : '';
  }

  isProductiveDate(month: number, dayIndex: number): boolean {
    const day = dayIndex + 1;
      
    return this.selectedProductDates[this.direction]?.find((date) => { return date.getDate() === day && date.getMonth() === month})
      ? true
      : false;
  }

  isUnproductiveDate(month: number, dayIndex: number): boolean {
    const day = dayIndex + 1;
    return this.selectedNonProductDates[this.direction]?.find(
      (date) => date.getDate() === day && date.getMonth() === month,
    )
      ? true
      : false;
  }

  isBlockedDate(month: number, day: number): boolean {
    return !this.isProductiveDate(month, day) && !this.isUnproductiveDate(month, day);
  }

  selectDate(month: number, dayIndex: number): void {
    const day = dayIndex + 1;
    if (this.isProductiveSelected) {
      //Check the date is already selected
      if (
        this.selectedProductDates[this.direction].find(
          (date) => date.getDate() === day && date.getMonth() === month,
        )
      ) {
        this.selectedProductDates[this.direction] = this.selectedProductDates[
          this.direction
        ].filter((date) => {
          return date.getDate() !== day || date.getMonth() !== month;
        });
      } else {
        //Check if the date is already selected as non-productive
        if (
          this.selectedNonProductDates[this.direction].find(
            (date) => date.getDate() === day && date.getMonth() === month,
          )
        ) {
          this.selectedNonProductDates[this.direction] = this.selectedNonProductDates[
            this.direction
          ].filter((date) => {
            return date.getDate() !== day || date.getMonth() !== month;
          });
        }
        //Add the date to the selected dates
        // Check the limitation
        if (this.selectedProductDates[this.direction].length >= this.allowedAmountDatesProductive) {
          this.showNotification(
            `The productive day should less than ${this.allowedAmountDatesProductive}`,
            'warning',
          );
          return;
        }
        this.selectedProductDates[this.direction].push(new Date(this.year, month, day));
      }
    } else {
      //Check if the date is already selected
      if (
        this.selectedNonProductDates[this.direction].find(
          (date) => date.getDate() === day && date.getMonth() === month,
        )
      ) {
        this.selectedNonProductDates[this.direction] = this.selectedNonProductDates[
          this.direction
        ].filter((date) => date.getDate() !== day || date.getMonth() !== month);
      } else {
        //Check if the date is already selected as productive
        if (
          this.selectedProductDates[this.direction].find(
            (date) => date.getDate() === day && date.getMonth() === month,
          )
        ) {
          this.selectedProductDates[this.direction] = this.selectedProductDates[
            this.direction
          ].filter((date) => {
            return date.getDate() !== day || date.getMonth() !== month;
          });
        }

        //Check the limitation
        if (
          this.selectedNonProductDates[this.direction].length >=
          this.allowedAmountDatesNonProductive
        ) {
          this.showNotification(
            `The productive day should less than ${this.allowedAmountDatesNonProductive}`,
            'warning',
          );
          return;
        }
        //Add the date to the selected dates
        this.selectedNonProductDates[this.direction].push(new Date(this.year, month, day));
      }
    }
  }

  selectTravelType(isProductive: boolean): void {
    this.isProductiveSelected = isProductive;
  }

  async changeOption(
    event: SdxTabsCustomEvent<{ target: { value: Directions } }> | SdxInputCustomEvent<unknown>,
  ): Promise<void> {
    this.direction = (event as SdxInputCustomEvent<unknown>).target.value as unknown as Directions;
    switch (Number(this.direction.toString())) {
      case 0:
        {
          this.location = 'Latvia';
          this.isBaseLocationCH = true;
          this.isProductiveSelected = true;
        }
        break;
      case 1:
        {
          this.location = 'Netherlands';
          this.isBaseLocationCH = true;
          this.isProductiveSelected = true;
        }
        break;
      case 2:
        {
          this.location = 'Switzerland';
          this.isBaseLocationCH = false;
        }
        break;
      case 3:
        {
          this.location = 'Netherlands';
          this.isBaseLocationCH = false;
        }
        break;
      case 4:
        {
          this.location = 'Switzerland';
          this.isBaseLocationCH = false;
        }
        break;
      case 5:
        {
          this.location = 'Latvia';
          this.isBaseLocationCH = false;
        }
        break;
    }
    
    this.selectedDate = null
    this.travelRequests = [];
    await this.loadTravelRequests(this.direction);
  }

  updateWorkingDays(): void {
    this.workingDays = 0;
    this.dates.forEach((month) => {
      month.forEach((day) => (day === 1 ? this.workingDays++ : {}));
    });
  }

  getDaysInMonth() {
    return this.months.map((m) =>
      new Date(new Date(this.year, 1, 1).getFullYear(), this.months.indexOf(m) + 1, 0).getDate(),
    );
  }

  getFirstDayOfMonth() {
    return this.months.map((m) =>
      new Date(new Date(this.year, 1, 1).getFullYear(), this.months.indexOf(m), 1).getDay() > 0
        ? new Date(new Date(this.year, 1, 1).getFullYear(), this.months.indexOf(m), 1).getDay() - 1
        : 6,
    );
  }

  enableEditMode(): void {
    this.isEditable = true;
    this.stateBeforeEditProductDates = structuredClone(this.selectedProductDates);
    this.stateBeforeEditNonProductDates = structuredClone(this.selectedNonProductDates);
  }

  disableEditMode(noRevert?: boolean): void {
    this.isShowAllocationDaysSelected = false;
    this.isEditable = false;
    if (noRevert)
      return

    console.log(noRevert)
    this.selectedProductDates = structuredClone(this.stateBeforeEditProductDates);
    this.selectedNonProductDates = structuredClone(this.stateBeforeEditNonProductDates);
  }

  submitChanges(): void {
    this.isLoading = true;
    const beginOfYear = new Date(new Date().getFullYear(), 0, 1);
    const endOfYear = new Date(new Date().getFullYear(), 11, 31);
    const productiveDates = this.selectedProductDates[this.direction].map((date) =>
      dayjs(date).format('YYYY-MM-DD'),
    );
    const unproductiveDates = this.selectedNonProductDates[this.direction].map((date) =>
      dayjs(date).format('YYYY-MM-DD'),
    );

    const travelRuleSetting: TravelRuleSettingsType = {
      applicableFrom: beginOfYear,
      applicableUntil: endOfYear,
      allowedUnproductiveDates: unproductiveDates,
      allowedProductiveDates: productiveDates,
      direction: this.direction,
    };


    this.contingentsApiService
      .updateTravelRuleSettingsContingent(this.userDetail().id, travelRuleSetting)
      .then(() => {
        this.showNotification('Changes submitted successfully', 'confirmation');
        this.fetchingService.contingent.refetchContingentData();
        this.disableEditMode(true);
      })
      .catch((e) => {
        this.showNotification(`Failed to submit due to ${e}`, 'warning');
        this.disableEditMode(false)

      })
      .finally(() => {
        this.isLoading = false;
      });
      
  }

  initializeEmptyDates(): number[][] {
    const months = 12;
    const daysInMonth = 31;
    return Array.from({ length: months }, () => Array(daysInMonth).fill(0));
  }

  syncDates(): void {
    if (!this.datesData || this.datesData.length === 0) return;

    Object.keys(this.selectedProductDates).forEach((direction) => {
      this.selectedNonProductDates[direction as unknown as Directions] =
        this.datesData
          .find((rule) => Number(Directions[rule.direction as Directions]) === Number(direction))
          ?.allowedUnproductiveDates?.map((date) => new Date(date)) || [];

      this.selectedProductDates[direction as unknown as Directions] =
        this.datesData
          .find((rule) => Number(Directions[rule.direction as Directions]) === Number(direction))
          ?.allowedProductiveDates?.map((date) => new Date(date)) || [];
    });
  }

  showNotification(message: string, modifier: 'general' | 'confirmation' | 'warning') {
    const headerElement = document.querySelector('sdx-header');
    if (headerElement) {
      headerElement.showToast({
        description: message,
        type: modifier,
      });
    }
  }

  isDateInPast(month: number, day: number) : boolean{
    const dayDate = this.getDate(month, day);
    const today = new Date();
    
    // Set time to 00:00:00 to compare only dates
    today.setHours(0, 0, 0, 0);
    dayDate.setHours(0, 0, 0, 0);
    return dayDate < today; 

  }

  isTraveledOnDay(month: number, day: number) : boolean{
    if (!this.travelRequests || this.travelRequests.length === 0) return false; // if you have a data property

    // Build a Date object for the requested day
    const dayDate = this.getDate(month, day);
    const q = this.travelRequests.some((req: CommunityCalendarTravelRequestDto) => {
      const start = new Date(req.departureDate);
      const end = new Date(req.returnDate);
      
      return dayDate >= start && dayDate <= end;
    });
    return q
  }

  async loadTravelRequests(direction: Directions) {
   
    this.travelRequestsLoading = true;
    const userGroup = this.contingentUsergroup().find(usergroup => 
      usergroup.travelRuleSettings?.some(rule => {return rule.direction === Directions[direction]})
    );
    const userGroupId = userGroup?.id

    if(userGroupId == undefined){
      this.travelRequests = [];
      this.travelRequestsLoading = false;
      return
    }
    
    try {
      const allRequests = await this.contingentsApiService.fetchContingentsTravelRequests(
        userGroupId,
        `${this.currentYear}-01-01`,
        `${this.currentYear}-12-31`
      );
      this.travelRequests = allRequests.filter(
        (req) => req.country === this.getCountryFromDirections(this.direction)
      );
    } catch {
      this.travelRequests = [];
      this.showNotification('Failed to load travel requests', 'warning');
      console.error('Failed to load travel requests');
    } finally {
      this.travelRequestsLoading = false;
      this.markTraveledDays()
    }
  }

  getCountryFromDirections(direction: Directions) : string{
    return Directions[direction].toString().split("_")[1];
  }

  getDate(month: number, day: number) : Date{
    const year = this.currentYear; // or pass as parameter if needed
    return new Date(year, month, day + 1);
  }

  initializeEmptyBooleanMatrix(): boolean[][] {
    return Array.from({ length: 12 }, () => Array(31).fill(false));
  }

  shouldDateBeReallocated(monthIndex: number, dayIndex: number): boolean {
    this.isProductiveDate(monthIndex, dayIndex)
    return this.isDateInPast(monthIndex, dayIndex) && !this.isTraveled[monthIndex][dayIndex] && this.isProductiveDate(monthIndex, dayIndex)
  }


  markTraveledDays(): void {
    if (!this.travelRequests) return;

    this.isTraveled = this.initializeEmptyBooleanMatrix();

    for (const req of this.travelRequests) {
      const start = new Date(req.departureDate);
      const end = new Date(req.returnDate);

      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        if (d.getFullYear() === this.currentYear) {
          const month = d.getMonth();
          const day = d.getDate() - 1;
          this.isTraveled[month][day] = true;
        }
      }
    }
  }
}



