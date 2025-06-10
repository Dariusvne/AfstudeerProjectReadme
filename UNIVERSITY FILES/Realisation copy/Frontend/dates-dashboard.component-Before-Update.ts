import { FormStyle, getLocaleMonthNames, TranslationWidth } from '@angular/common';
import { Component, effect, Signal, AfterViewInit } from '@angular/core';
import { Directions, TravelRuleSettingsType } from 'src/app/common/types/TravelRuleSettings';
import { UserDetailType } from 'src/app/common/types/UserDetail';
import { UserGroupType } from 'src/app/common/types/Usergroup';
import { FetchingService } from './../../services/api/fetch/fetching.service';
import dayjs from 'dayjs';
import { SdxInputCustomEvent, SdxTabsCustomEvent } from '@swisscom/sdx';
import { ContingentsApiService } from 'src/app/services/endpoints/contingents-api.service';

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
              <sdx-tabs-item [value]="0" label="CH to LV"></sdx-tabs-item>
              <sdx-tabs-item [value]="1" label="CH to NL"></sdx-tabs-item>
              <sdx-tabs-item [value]="2" label="LV to CH"></sdx-tabs-item>
              <sdx-tabs-item [value]="3" label="LV to NL"></sdx-tabs-item>
              <sdx-tabs-item [value]="4" label="NL to LV"></sdx-tabs-item>
              <sdx-tabs-item [value]="5" label="NL to CH"></sdx-tabs-item>
            </sdx-tabs>
          </div>
          <div class="d-flex flex-row gap-4 justify-content-between flex-wrap">
            <div class="d-flex flex-row justify-content-center w-100" style="flex:1">
              <h2 class="h2 m-0">{{ year }}</h2>
            </div>

            <div class="month-container">
              <div
                *ngFor="let month of months; let monthIndex = index"
                class="card month-card"
                [ngClass]="isCurrentMonth(monthIndex)"
              >
                <div class="h4">{{ month }}</div>
                <div class="calendar-weekdays">
                  <div class="day-name">
                    {{
                      '                                                                                                                                      '
                        | translate
                    }}
                  </div>
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
                    [attr.aria-label]="'Select day ' + (dayIndex + 1) + ' of ' + months[monthIndex]"
                    [ngClass]="{
                  'productive-hover': isProductiveSelected === true,
                  'not-allowed': isBlockedDate(monthIndex, dayIndex),
                  allowed: isProductiveDate(monthIndex, dayIndex),
                  'allowed-non-prod': isUnproductiveDate(monthIndex, dayIndex),
                }"
                    (click)="selectDate(monthIndex, dayIndex)"
                    (keydown.enter)="selectDate(monthIndex, dayIndex)"
                    (keydown.space)="selectDate(monthIndex, dayIndex); $event.preventDefault()"
                  >
                    {{ dayIndex + 1 }}
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
                        [ngClass]="{ selected: isProductiveSelected }"
                      >
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
                <div class="d-flex justify-content-end my-auto" style="flex:1">
                  <sdx-button
                    [disabled]="isLoading"
                    class="submit-button"
                    (click)="submitChanges()"
                    label="Save"
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
  styles: `
    h1 {
      padding-top: 3vw;
    }

    .month-container {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: center;
      gap: 1rem;
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
})
export class DatesDashboardComponent implements AfterViewInit {
  workingDays = 0;
  location = '';
  direction: Directions = Directions.CH_LV;

  year = new Date().getFullYear();
  currentYear = new Date().getFullYear();
  readonly months = getLocaleMonthNames('en-US', FormStyle.Standalone, TranslationWidth.Wide);

  daysInMonths: number[] = this.getDaysInMonth();
  firstMonthDay: number[] = this.getFirstDayOfMonth();
  Arr = Array;

  userDetail: Signal<UserDetailType>;
  contingentUsergroup: Signal<UserGroupType[]>;
  datesData: TravelRuleSettingsType[] = [];

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

  isCurrentMonth(month: string | number) {
    return new Date().getMonth() === Number(month) ? 'current' : '';
  }

  isProductiveDate(month: number, dayIndex: number): boolean {
    const day = dayIndex + 1;
    return this.selectedProductDates[this.direction]?.find(
      (date) => date.getDate() === day && date.getMonth() === month,
    )
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

  changeOption(
    event: SdxTabsCustomEvent<{ target: { value: Directions } }> | SdxInputCustomEvent<unknown>,
  ): void {
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
      })
      .catch((e) => {
        this.showNotification(`Failed to submit due to ${e}`, 'warning');
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
}
