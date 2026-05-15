import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ParkingLot } from '../../shared/models/parking-lot.model';

@Injectable({ providedIn: 'root' })
export class ParkingLotService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllApprovedLots(): Observable<ParkingLot[]> {
    return this.http.get<ParkingLot[]>(`${this.apiUrl}/api/lots`);
  }

  getNearbyLots(lat: number, lng: number, radius: number): Observable<ParkingLot[]> {
    return this.http.get<ParkingLot[]>(
      `${this.apiUrl}/api/lots/nearby?lat=${lat}&lng=${lng}&radius=${radius}`);
  }

  getLotsByCity(city: string): Observable<ParkingLot[]> {
    return this.getAllApprovedLots().pipe(
      map(lots => lots.filter(l =>
        l.city?.toLowerCase().includes(city.toLowerCase().trim())
      ))
    );
  }

  getPendingLots(): Observable<ParkingLot[]> {
    return this.http.get<ParkingLot[]>(`${this.apiUrl}/api/lots/pending`);
  }

  getLotById(id: number): Observable<ParkingLot> {
    return this.http.get<ParkingLot>(`${this.apiUrl}/api/lots/${id}`);
  }

  getLotsByManager(managerId: number): Observable<ParkingLot[]> {
    return this.http.get<ParkingLot[]>(`${this.apiUrl}/api/lots/manager/${managerId}`);
  }

  createLot(lot: ParkingLot): Observable<ParkingLot> {
    return this.http.post<ParkingLot>(`${this.apiUrl}/api/lots`, lot);
  }

  updateLot(id: number, lot: Partial<ParkingLot>): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(`${this.apiUrl}/api/lots/${id}`, lot);
  }

  syncSpotCount(id: number, totalSpots: number): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(
      `${this.apiUrl}/api/lots/${id}/sync-spots?totalSpots=${totalSpots}`, {});
  }

  approveLot(id: number): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(`${this.apiUrl}/api/lots/${id}/approve`, {});
  }

  toggleOpen(id: number): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(`${this.apiUrl}/api/lots/${id}/toggle`, {});
  }

  deleteLot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/lots/${id}`);
  }
}
