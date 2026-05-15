import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Spot, BulkSpotRequest } from '../../shared/models/spot.model';

@Injectable({ providedIn: 'root' })
export class SpotService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getSpotsByLot(lotId: number): Observable<Spot[]> {
    return this.http.get<Spot[]>(`${this.apiUrl}/api/spots/lot/${lotId}`);
  }

  getAvailableSpots(lotId: number): Observable<Spot[]> {
    return this.http.get<Spot[]>(`${this.apiUrl}/api/spots/lot/${lotId}/available`);
  }

  addBulkSpots(request: BulkSpotRequest): Observable<Spot[]> {
    return this.http.post<Spot[]>(`${this.apiUrl}/api/spots/bulk`, request);
  }

  addSpot(spot: Spot): Observable<Spot> {
    return this.http.post<Spot>(`${this.apiUrl}/api/spots`, spot);
  }

  updateSpot(id: number, spot: Partial<Spot>): Observable<Spot> {
    return this.http.put<Spot>(`${this.apiUrl}/api/spots/${id}`, spot);
  }

  deleteSpot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/spots/${id}`);
  }
}
