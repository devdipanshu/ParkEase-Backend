import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Payment } from '../../shared/models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  processPayment(payment: Payment): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/api/payments`, payment);
  }

  getMyPayments(userId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/api/payments/user/${userId}`);
  }

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/api/payments/${id}`);
  }

  getPaymentByBooking(bookingId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/api/payments/booking/${bookingId}`);
  }

  getRevenueByLot(lotId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/api/payments/revenue/lot/${lotId}`);
  }

  downloadReceipt(paymentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/api/payments/${paymentId}/receipt`, {
      responseType: 'blob'
    });
  }
}
