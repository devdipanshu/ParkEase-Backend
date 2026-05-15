import { Component, NgZone, OnInit } from '@angular/core';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { SpotService } from '../../core/services/spot.service';
import { BookingService } from '../../core/services/booking.service';
import { PaymentService } from '../../core/services/payment.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { AuthService } from '../../core/services/auth.service';
import { ParkingLot } from '../../shared/models/parking-lot.model';
import { Spot } from '../../shared/models/spot.model';
import { Booking } from '../../shared/models/booking.model';
import { Vehicle } from '../../shared/models/vehicle.model';
import { environment } from '../../../environments/environment';

declare var Razorpay: any;

@Component({
  selector: 'app-lot-detail',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, MatIconModule],
  templateUrl: './lot-detail.component.html'
})
export class LotDetailComponent implements OnInit {
  lot: ParkingLot | null = null;
  spots: Spot[] = [];
  allSpots: Spot[] = [];
  vehicles: Vehicle[] = [];
  loading = true;
  selectedSpot: Spot | null = null;
  showBookingForm = false;
  booking = { vehiclePlate: '', vehicleType: 'CAR', bookingType: 'PRE', startTime: '', endTime: '' };
  errorMsg = '';
  submitting = false;

  showPaymentForm = false;
  createdBooking: Booking | null = null;
  paymentMode = 'CARD';
  paymentSubmitting = false;
  paymentError = '';
  estimatedAmount = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private lotService: ParkingLotService,
    private spotService: SpotService,
    private bookingService: BookingService,
    private paymentService: PaymentService,
    private vehicleService: VehicleService,
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const userId = this.authService.getUserId();

    this.lotService.getLotById(id).subscribe({
      next: (data) => { this.lot = data; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.refreshSpots(id);

    this.vehicleService.getMyVehicles(userId).subscribe({
      next: (data) => this.vehicles = data.filter(v => v.isActive !== false),
      error: () => {}
    });
  }

  refreshSpots(lotId?: number) {
    const id = lotId ?? Number(this.route.snapshot.paramMap.get('id'));
    this.spotService.getAvailableSpots(id).subscribe({
      next: (data) => this.spots = data,
      error: () => {}
    });
    this.spotService.getSpotsByLot(id).subscribe({
      next: (data) => {
        this.allSpots = data;
        // Sync lot entity counts so search results stay current
        this.lotService.syncSpotCount(id, data.length).subscribe({ error: () => {} });
      },
      error: () => {}
    });
  }

  selectVehicle(v: Vehicle) {
    this.booking.vehiclePlate = v.licensePlate;
    this.booking.vehicleType = v.vehicleType;
  }

  selectSpot(spot: Spot) {
    this.selectedSpot = spot;
    this.showBookingForm = true;
    this.showPaymentForm = false;
    this.errorMsg = '';
  }

  submitBooking() {
    if (!this.selectedSpot || !this.lot) return;
    if (!this.booking.vehiclePlate.trim()) {
      this.errorMsg = 'Please enter a vehicle plate or select a registered vehicle.';
      return;
    }
    if (!this.booking.startTime || !this.booking.endTime) {
      this.errorMsg = 'Please select start and end time.';
      return;
    }
    if (new Date(this.booking.endTime) <= new Date(this.booking.startTime)) {
      this.errorMsg = 'End time must be after start time.';
      return;
    }
    this.submitting = true;
    this.errorMsg = '';

    this.bookingService.createBooking({
      userId: this.authService.getUserId(),
      lotId: this.lot.lotId!,
      spotId: this.selectedSpot.spotId!,
      vehiclePlate: this.booking.vehiclePlate,
      vehicleType: this.booking.vehicleType,
      bookingType: this.booking.bookingType,
      startTime: this.booking.startTime,
      endTime: this.booking.endTime
    }).subscribe({
      next: (b) => {
        this.createdBooking = b;
        this.submitting = false;
        if (this.booking.bookingType === 'PRE') {
          this.estimatedAmount = this.calcAmount();
          this.showBookingForm = false;
          this.showPaymentForm = true;
        } else {
          this.router.navigate(['/driver/my-bookings']);
        }
      },
      error: (err) => {
        this.errorMsg = err?.error?.message || 'Booking failed. Spot may already be taken.';
        this.submitting = false;
      }
    });
  }

  openRazorpay() {
    if (!this.createdBooking || !this.lot) return;
    this.paymentError = '';

    const amount = this.createdBooking.totalAmount || this.estimatedAmount;
    const bookingId = this.createdBooking.bookingId!;

    const options = {
      key: environment.razorpayKey,
      amount: Math.round(amount * 100), // paise
      currency: 'INR',
      name: 'ParkEase',
      description: `Booking #${bookingId} — Spot ${this.selectedSpot?.spotNumber}`,
      image: '/favicon.ico',
      handler: (response: any) => {
        this.ngZone.run(() => {
          this.paymentSubmitting = true;
          this.paymentService.processPayment({
            bookingId,
            userId: this.authService.getUserId(),
            lotId: this.lot!.lotId!,
            amount,
            mode: 'RAZORPAY',
            transactionId: response.razorpay_payment_id
          }).subscribe({
            next: () => this.router.navigate(['/driver/my-bookings']),
            error: () => {
              this.paymentError = 'Payment received but confirmation failed. Contact support with ID: ' + response.razorpay_payment_id;
              this.paymentSubmitting = false;
            }
          });
        });
      },
      prefill: {
        name: this.authService.getFullName() || '',
        email: this.authService.getEmail() || ''
      },
      theme: { color: '#3b82f6' },
      modal: {
        ondismiss: () => {
          this.ngZone.run(() => {
            this.paymentError = 'Payment was cancelled. Your booking is reserved — complete payment to confirm your spot.';
          });
        }
      }
    };

    const rzp = new Razorpay(options);
    rzp.on('payment.failed', (response: any) => {
      this.ngZone.run(() => {
        this.paymentError = 'Payment failed: ' + (response.error?.description || 'Unknown error');
        this.bookingService.cancelBooking(bookingId).subscribe({
          next: () => {
            this.showPaymentForm = false;
            this.createdBooking = null;
            this.errorMsg = 'Payment failed. Booking cancelled — spot is now free. Please try again.';
            this.paymentError = '';
          },
          error: () => {}
        });
      });
    });
    rzp.open();
  }

  cancelPayment() {
    this.router.navigate(['/driver/my-bookings']);
  }

  private calcAmount(): number {
    if (!this.selectedSpot || !this.booking.startTime || !this.booking.endTime) return 0;
    const start = new Date(this.booking.startTime).getTime();
    const end = new Date(this.booking.endTime).getTime();
    const hours = Math.ceil((end - start) / (1000 * 60 * 60));
    return hours * (this.selectedSpot.pricePerHour || 0);
  }
}
