import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Booking, BookingRequest } from '../../shared/models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createBooking(request: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/api/bookings`, request);
  }

  getMyBookings(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/api/bookings/user/${userId}`);
  }

  getBookingsByLot(lotId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/api/bookings/lot/${lotId}`);
  }

  checkIn(bookingId: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/api/bookings/${bookingId}/checkin`, {});
  }

  checkOut(bookingId: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/api/bookings/${bookingId}/checkout`, {});
  }

  cancelBooking(bookingId: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/api/bookings/${bookingId}/cancel`, {});
  }

  extendBooking(bookingId: number, newEndTime: string): Observable<Booking> {
    return this.http.put<Booking>(
      `${this.apiUrl}/api/bookings/${bookingId}/extend`, { newEndTime });
  }
}
