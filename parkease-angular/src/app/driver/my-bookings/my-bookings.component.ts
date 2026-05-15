import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { BookingService } from '../../core/services/booking.service';
import { PaymentService } from '../../core/services/payment.service';
import { AuthService } from '../../core/services/auth.service';
import { Booking } from '../../shared/models/booking.model';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, FormsModule, MatIconModule],
  templateUrl: './my-bookings.component.html'
})
export class MyBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  activeTab = 'ALL';
  loading = true;
  error = '';
  successMsg = '';

  tabs = ['ALL', 'RESERVED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

  showExtendModal = false;
  extendBookingId = 0;
  newEndTime = '';
  extendSubmitting = false;

  showPaymentModal = false;
  paymentBooking: Booking | null = null;
  paymentMode = 'CARD';
  paymentSubmitting = false;
  paymentError = '';
  checkingPayment = false;

  // Tracks booking ID when payment modal is opened from checkout flow
  private pendingCheckoutBookingId: number | null = null;

  constructor(
    private bookingService: BookingService,
    private paymentService: PaymentService,
    private authService: AuthService
  ) {}

  ngOnInit() { this.loadData(); }

  loadData() {
    this.loading = true;
    this.error = '';
    const userId = this.authService.getUserId();
    this.bookingService.getMyBookings(userId).subscribe({
      next: (data) => {
        this.bookings = data.sort((a, b) => {
          const timeDiff = new Date(b.startTime).getTime() - new Date(a.startTime).getTime();
          return timeDiff !== 0 ? timeDiff : (b.bookingId ?? 0) - (a.bookingId ?? 0);
        });
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }

  get filteredBookings(): Booking[] {
    if (this.activeTab === 'ALL') return this.bookings;
    return this.bookings.filter(b => b.status === this.activeTab);
  }

  checkIn(id: number) {
    this.successMsg = '';
    this.bookingService.checkIn(id).subscribe({
      next: () => { this.successMsg = 'Checked in successfully!'; this.loadData(); },
      error: (err) => { this.successMsg = err?.error?.message || 'Check-in failed.'; }
    });
  }

  checkOut(id: number) {
    this.successMsg = '';
    const booking = this.bookings.find(b => b.bookingId === id);

    if (booking?.bookingType === 'PRE') {
      this.checkingPayment = true;
      this.paymentService.getPaymentByBooking(id).subscribe({
        next: (payment) => {
          this.checkingPayment = false;
          if (payment && payment.status === 'PAID') {
            this.doCheckOut(id, booking);
          } else {
            // Payment exists but failed/pending — use the recorded amount
            const amount = payment?.amount ?? this.calcBookingAmount(booking);
            this.pendingCheckoutBookingId = id;
            this.paymentBooking = { ...booking, totalAmount: amount };
            this.paymentMode = 'CARD';
            this.paymentError = 'Previous payment failed. Please pay to proceed with checkout.';
            this.showPaymentModal = true;
          }
        },
        error: () => {
          this.checkingPayment = false;
          // No payment record — calculate from booking duration and price
          const amount = this.calcBookingAmount(booking!);
          this.pendingCheckoutBookingId = id;
          this.paymentBooking = { ...booking!, totalAmount: amount };
          this.paymentMode = 'CARD';
          this.paymentError = 'Payment is required before checkout for pre-booked slots.';
          this.showPaymentModal = true;
        }
      });
    } else {
      this.doCheckOut(id, booking);
    }
  }

  private doCheckOut(id: number, booking: Booking | undefined) {
    this.bookingService.checkOut(id).subscribe({
      next: (b) => {
        this.loadData();
        if (booking?.bookingType === 'WALK_IN' && b.totalAmount && b.totalAmount > 0) {
          this.pendingCheckoutBookingId = null;
          this.paymentBooking = b;
          this.paymentMode = 'CARD';
          this.paymentError = '';
          this.showPaymentModal = true;
          this.successMsg = `Checked out! Please complete payment of Rs.${b.totalAmount}`;
        } else {
          this.successMsg = `Checked out successfully! Amount: Rs.${b.totalAmount}`;
        }
      },
      error: (err) => { this.successMsg = err?.error?.message || 'Checkout failed.'; }
    });
  }

  private calcBookingAmount(booking: Booking): number {
    if (!booking.startTime || !booking.endTime || !booking.pricePerHour) return 0;
    const ms = new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime();
    const hours = Math.max(1, Math.ceil(ms / (1000 * 60 * 60)));
    return hours * booking.pricePerHour;
  }

  cancel(id: number) {
    if (!confirm('Cancel this booking?')) return;
    this.successMsg = '';
    this.bookingService.cancelBooking(id).subscribe({
      next: () => { this.successMsg = 'Booking cancelled.'; this.loadData(); },
      error: () => { this.successMsg = 'Cancel failed.'; }
    });
  }

  openExtendModal(booking: Booking) {
    this.extendBookingId = booking.bookingId!;
    this.newEndTime = booking.endTime;
    this.showExtendModal = true;
  }

  submitExtend() {
    if (!this.newEndTime) return;
    this.extendSubmitting = true;
    this.bookingService.extendBooking(this.extendBookingId, this.newEndTime).subscribe({
      next: () => {
        this.successMsg = 'Booking extended successfully!';
        this.showExtendModal = false;
        this.extendSubmitting = false;
        this.loadData();
      },
      error: (err) => {
        this.successMsg = err?.error?.message || 'Extend failed.';
        this.extendSubmitting = false;
      }
    });
  }

  submitPayment() {
    if (!this.paymentBooking) return;
    this.paymentSubmitting = true;
    this.paymentError = '';

    this.paymentService.processPayment({
      bookingId: this.paymentBooking.bookingId!,
      userId: this.authService.getUserId(),
      lotId: this.paymentBooking.lotId,
      amount: this.paymentBooking.totalAmount!,
      mode: this.paymentMode
    }).subscribe({
      next: () => {
        this.paymentSubmitting = false;
        this.showPaymentModal = false;

        const checkoutId = this.pendingCheckoutBookingId;
        const checkoutBooking = this.paymentBooking;
        this.pendingCheckoutBookingId = null;
        this.paymentBooking = null;

        if (checkoutId !== null) {
          // Payment was required for checkout — now proceed with checkout
          this.successMsg = 'Payment successful! Processing checkout...';
          this.doCheckOut(checkoutId, checkoutBooking ?? undefined);
        } else {
          // Standalone payment (WALK_IN after checkout)
          this.successMsg = 'Payment successful! Receipt available in Payment History.';
          this.loadData();
        }
      },
      error: () => {
        this.paymentError = 'Payment failed. Please try again.';
        this.paymentSubmitting = false;
      }
    });
  }

  closePaymentModal() {
    this.showPaymentModal = false;
    this.paymentError = '';
    this.pendingCheckoutBookingId = null;
    this.paymentBooking = null;
  }
}
