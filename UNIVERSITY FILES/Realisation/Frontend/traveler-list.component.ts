import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface TravelerItem {
  id: string;
  name: string;
  department: string;
  totalDays: string;
  hasImage: boolean;
}

@Component({
  selector: 'app-traveler-list',
  template: `
    <!-- Title -->
    <div class="pb-4">
      {{ title }}
    </div>

    <!-- Traveler list -->
    <div
      class="traveler-list-container d-flex flex-column gap-4"
      *ngIf="travelers && travelers.length > 0"
    >
      <div
        class="traveler-card d-flex flex-row gap-4"
        *ngFor="let traveler of travelers"
        [ngClass]="selectedTravelerId === traveler.id ? 'selected' : ''"
        role="button"
        tabindex="0"
        (click)="handleSelect(traveler.id)"
        (keydown.enter)="handleSelect(traveler.id)"
        (keydown.space)="handleSelect(traveler.id)"
      >
        <div class="profile-img my-auto text-align-center d-flex flex-column justify-content-center">
          <!-- production image or fallback icon -->
          <ng-container *ngIf="isProduction">
            <img
              *ngIf="traveler.hasImage"
              [src]="getUserPictureById(traveler.id)"
              alt="profile"
              (error)="traveler.hasImage = false"
            />
            <sdx-icon *ngIf="!traveler.hasImage" icon-name="icon-account" size="4" />
          </ng-container>
          <sdx-icon *ngIf="!isProduction" icon-name="icon-account" size="4" />
        </div>

        <div class="d-flex flex-column gap-0 my-auto justify-content-center">
          <h5 class="h5 m-0 p-0">{{ traveler.name }} - {{ traveler.department }}</h5>
          <p class="text-small p-0">{{ traveler.totalDays }}</p>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      class="mx-auto text-center"
      *ngIf="!travelers || travelers.length === 0"
    >
      <h5 class="h5 text-center p-0 pt-4 m-0">
        There is no one traveling to {{ destinationName }} on this date
      </h5>
      <p class="text-standard text-center p-0 m-0">
        {{ emptyStateSubtitle }}
      </p>
    </div>
  `,
  styles: [
    `
      .traveler-list-container {
        overflow-y: auto;
        padding-bottom: 1rem;
      }
      .traveler-card {
        background-color: var(--sdx-color-horizon);
        padding: 24px;
        border-radius: 12px;
        cursor: pointer;
        outline: none;
      }
      .traveler-card:focus {
        box-shadow: 0 0 0 3px var(--sdx-color-focus);
      }
      .traveler-card.selected {
        background-color: var(--sdx-color-blue-tint-3);
      }
      .profile-img {
        border-radius: 50%;
        overflow: hidden;
        min-width: 48px;
        min-height: 48px;
      }
      .profile-img img {
        object-fit: cover;
        min-width: 48px;
        min-height: 48px;
      }
    `,
  ],
})
export class TravelerListComponent {
  /** Title to display above the list (e.g. formatted date) */
  @Input() title = '';

  /** Array of traveler items to render */
  @Input() travelers: TravelerItem[] = [];

  /** Currently selected traveler ID to highlight */
  @Input() selectedTravelerId: string | null = null;

  /** Flag to determine production mode for image rendering */
  @Input() isProduction = false;

  /** Function to retrieve user picture URL by employee ID */
  @Input() getUserPictureById: (id: string) => string = () => '';

  /** Name of the destination for empty state */
  @Input() destinationName = '';

  /** Subtitle text for empty state */
  @Input() emptyStateSubtitle = 'Please try another date';

  /** Emits when a traveler card is selected */
  @Output() selectTraveler = new EventEmitter<string>();

  /** Internal handler to unify click and keyboard events */
  handleSelect(id: string) {
    this.selectTraveler.emit(id);
  }
}
