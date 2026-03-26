import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface TrackingIssue {
  severity: string;
  title: string;
  detail: string;
  owner: string;
  action: string;
}

export interface TrackingEvent {
  label: string;
  location: string;
  timestamp: string;
}

export interface Shipment {
  reference: string;
  client: string;
  mode: string;
  status: string;
  progress: number;
  eta: string;
  currentLocation: string;
  destination: string;
  summary?: string;
  nextAction?: string;
  supportOwner?: string;
  events: TrackingEvent[];
  issues?: TrackingIssue[];
}

export interface TrackingResponse {
  sampleIssueReference: string;
  shipment: Shipment;
}

export interface ApiMessageResponse {
  message: string;
  redirectTo?: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  track(reference: string): Observable<TrackingResponse> {
    return this.http.get<TrackingResponse>(
      `/api/track?reference=${encodeURIComponent(reference)}`
    );
  }

  submitContact(payload: {
    name: string;
    email: string;
    company: string;
    message: string;
  }): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>('/api/contact', payload);
  }

  submitQuote(payload: {
    company: string;
    contactName: string;
    email: string;
    serviceType: string;
    origin: string;
    destination: string;
    shipmentType: string;
    cargoDetails: string;
  }): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>('/api/quotes', payload);
  }

  submitSignup(payload: {
    name: string;
    email: string;
    company: string;
    phone: string;
    interest: string;
    password: string;
  }): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>('/api/signup', payload);
  }
}
