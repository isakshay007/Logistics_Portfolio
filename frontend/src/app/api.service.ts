import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

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
  shipment: Shipment;
}

export interface ApiMessageResponse {
  message: string;
  redirectTo?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  email: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly gatewayBaseUrl = environment.gatewayBaseUrl;
  private authToken: string | null = null;

  track(reference: string): Observable<TrackingResponse> {
    return this.http.get<TrackingResponse>(
      `${this.gatewayBaseUrl}/api/v1/shipments/track?reference=${encodeURIComponent(reference)}`
    );
  }

  submitContact(payload: {
    name: string;
    email: string;
    company: string;
    message: string;
  }): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>(
      `${this.gatewayBaseUrl}/api/v1/contacts`,
      payload
    );
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
    return this.http.post<ApiMessageResponse>(
      `${this.gatewayBaseUrl}/api/v1/quotes`,
      payload
    );
  }

  submitSignup(payload: {
    name: string;
    email: string;
    company: string;
    phone: string;
    interest: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.gatewayBaseUrl}/api/v1/auth/signup`,
      payload
    ).pipe(
      tap((response) => {
        this.authToken = response.token;
      })
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.gatewayBaseUrl}/api/v1/auth/login`,
      { email, password }
    ).pipe(
      tap((response) => {
        this.authToken = response.token;
      })
    );
  }

  getAuthToken(): string | null {
    return this.authToken;
  }
}
