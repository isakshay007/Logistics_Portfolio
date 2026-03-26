import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, Shipment } from './api.service';

type NoticeType = 'success' | 'error';

interface Notice {
  type: NoticeType;
  text: string;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent {
  private readonly api = inject(ApiService);

  navOpen = false;
  trackingReference = '';
  trackingLoading = false;
  trackingError = '';
  trackingShipment: Shipment | null = null;

  quoteForm = {
    company: '',
    contactName: '',
    email: '',
    serviceType: '',
    origin: '',
    destination: '',
    shipmentType: '',
    cargoDetails: '',
  };

  contactForm = {
    name: '',
    email: '',
    company: '',
    message: '',
  };

  notice: Notice | null = null;
  private noticeTimer: ReturnType<typeof setTimeout> | null = null;

  toggleNav(): void {
    this.navOpen = !this.navOpen;
  }

  closeNav(): void {
    this.navOpen = false;
  }

  trackShipment(): void {
    if (!this.trackingReference.trim()) {
      this.showNotice('error', 'Enter a shipment reference to continue.');
      return;
    }

    this.trackingLoading = true;
    this.trackingError = '';
    this.trackingShipment = null;

    this.api.track(this.trackingReference.trim()).subscribe({
      next: (response) => {
        this.trackingShipment = response.shipment;
        this.trackingLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.trackingLoading = false;
        this.trackingError =
          error.error?.message || 'Unable to retrieve shipment details.';
        this.showNotice('error', this.trackingError);
      },
    });
  }

  submitQuote(): void {
    this.api.submitQuote(this.quoteForm).subscribe({
      next: (response) => {
        this.showNotice('success', response.message);
        this.quoteForm = {
          company: '',
          contactName: '',
          email: '',
          serviceType: '',
          origin: '',
          destination: '',
          shipmentType: '',
          cargoDetails: '',
        };
      },
      error: (error: HttpErrorResponse) => {
        this.showNotice(
          'error',
          error.error?.message || 'Quote request failed. Please retry.'
        );
      },
    });
  }

  submitContact(): void {
    this.api.submitContact(this.contactForm).subscribe({
      next: (response) => {
        this.showNotice('success', response.message);
        this.contactForm = {
          name: '',
          email: '',
          company: '',
          message: '',
        };
      },
      error: (error: HttpErrorResponse) => {
        this.showNotice(
          'error',
          error.error?.message || 'Contact submission failed. Please retry.'
        );
      },
    });
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  issueSeverityClass(value: string): string {
    const normalized = value.toLowerCase();
    if (normalized === 'high') {
      return 'issue-high';
    }
    if (normalized === 'medium') {
      return 'issue-medium';
    }
    return 'issue-low';
  }

  private showNotice(type: NoticeType, text: string): void {
    this.notice = { type, text };

    if (this.noticeTimer) {
      clearTimeout(this.noticeTimer);
    }

    this.noticeTimer = setTimeout(() => {
      this.notice = null;
    }, 3600);
  }
}
