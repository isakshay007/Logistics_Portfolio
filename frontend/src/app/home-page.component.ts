import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef);

  navOpen = false;
  trackingReference = '';
  trackingLoading = false;
  trackingError = '';
  trackingShipment: Shipment | null = null;
  isQuoteSubmitting = false;
  quoteStatusType: NoticeType | null = null;
  quoteStatusMessage = '';
  isContactSubmitting = false;
  contactStatusType: NoticeType | null = null;
  contactStatusMessage = '';

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
      this.trackingError = 'Enter a shipment reference to continue.';
      this.trackingShipment = null;
      this.cdr.detectChanges();
      return;
    }

    this.trackingLoading = true;
    this.trackingError = '';
    this.trackingShipment = null;

    this.api.track(this.trackingReference.trim()).subscribe({
      next: (response) => {
        this.trackingLoading = false;
        this.trackingError = '';
        this.trackingShipment = response.shipment;
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.trackingLoading = false;
        this.trackingShipment = null;
        this.trackingError =
          error.error?.message || 'Unable to retrieve shipment details.';
        this.showNotice('error', this.trackingError);
        this.cdr.detectChanges();
      },
    });
  }

  submitQuote(): void {
    if (this.isQuoteSubmitting) {
      return;
    }

    this.isQuoteSubmitting = true;
    this.quoteStatusType = null;
    this.quoteStatusMessage = '';

    this.api.submitQuote(this.quoteForm).subscribe({
      next: (response) => {
        this.isQuoteSubmitting = false;
        this.quoteStatusType = 'success';
        this.quoteStatusMessage = response.message || 'Quote submitted successfully.';
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
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.isQuoteSubmitting = false;
        this.quoteStatusType = 'error';
        this.quoteStatusMessage =
          error.error?.message || 'Quote request failed. Please retry.';
        this.cdr.detectChanges();
      },
    });
  }

  submitContact(): void {
    if (this.isContactSubmitting) {
      return;
    }

    this.isContactSubmitting = true;
    this.contactStatusType = null;
    this.contactStatusMessage = '';

    this.api.submitContact(this.contactForm).subscribe({
      next: (response) => {
        this.isContactSubmitting = false;
        this.contactStatusType = 'success';
        this.contactStatusMessage =
          response.message || 'Message sent successfully.';
        this.contactForm = {
          name: '',
          email: '',
          company: '',
          message: '',
        };
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.isContactSubmitting = false;
        this.contactStatusType = 'error';
        this.contactStatusMessage =
          error.error?.message || 'Contact submission failed. Please retry.';
        this.cdr.detectChanges();
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
