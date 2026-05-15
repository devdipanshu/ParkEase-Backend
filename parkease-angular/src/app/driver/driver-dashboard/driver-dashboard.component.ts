import { Component, OnInit } from '@angular/core';
import { NgIf, NgFor, DatePipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { BookingService } from '../../core/services/booking.service';
import { NotificationService } from '../../core/services/notification.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { AuthService } from '../../core/services/auth.service';
import { Booking } from '../../shared/models/booking.model';
import { Notification } from '../../shared/models/notification.model';
import { Vehicle } from '../../shared/models/vehicle.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, SlicePipe, RouterLink, MatIconModule],
  templateUrl: './driver-dashboard.component.html'
})
export class DriverDashboardComponent implements OnInit {
  bookings: Booking[] = [];
  notifications: Notification[] = [];
  vehicles: Vehicle[] = [];
  unreadCount = 0;
  displayName = '';
  loading = true;
  error = '';
  cancellingId: number | null = null;

  quickLinks = [
    { icon: 'manage_search',    title: 'Find Parking',  desc: 'Search nearby lots',          route: '/driver/search-lots' },
    { icon: 'document_scanner', title: 'My Bookings',   desc: 'View and manage bookings',    route: '/driver/my-bookings' },
    { icon: 'directions_car',   title: 'My Vehicles',   desc: 'Manage your vehicles',        route: '/driver/my-vehicles' },
    { icon: 'add_card',         title: 'Payments',      desc: 'View payment history',        route: '/driver/payments' }
  ];

  constructor(
    private bookingService: BookingService,
    private notificationService: NotificationService,
    private vehicleService: VehicleService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.displayName = this.authService.getFullName() || this.authService.getEmail() || '';
    const userId = this.authService.getUserId();

    this.notificationService.getUnreadCount(userId).subscribe({
      next: res => this.unreadCount = res.unreadCount,
      error: () => {}
    });

    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = '';
    const userId = this.authService.getUserId();

    forkJoin({
      bookings: this.bookingService.getMyBookings(userId),
      notifications: this.notificationService.getMyNotifications(userId),
      vehicles: this.vehicleService.getMyVehicles(userId)
    }).subscribe({
      next: ({ bookings, notifications, vehicles }) => {
        this.bookings = bookings.sort((a, b) => {
          const timeDiff = new Date(b.startTime).getTime() - new Date(a.startTime).getTime();
          return timeDiff !== 0 ? timeDiff : (b.bookingId ?? 0) - (a.bookingId ?? 0);
        });
        this.notifications = notifications
          .sort((a, b) => new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime())
          .slice(0, 5);
        this.vehicles = vehicles.filter(v => v.isActive);
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load dashboard. Please try again.'; this.loading = false; }
    });
  }

  get activeBooking(): Booking | null {
    return this.bookings.find(b => b.status === 'ACTIVE' || b.status === 'RESERVED') ?? null;
  }

  get totalBookings()     { return this.bookings.length; }
  get activeBookings()    { return this.bookings.filter(b => b.status === 'RESERVED' || b.status === 'ACTIVE').length; }
  get completedBookings() { return this.bookings.filter(b => b.status === 'COMPLETED').length; }

  cancelBooking(bookingId: number) {
    this.cancellingId = bookingId;
    this.bookingService.cancelBooking(bookingId).subscribe({
      next: () => {
        this.cancellingId = null;
        this.loadData();
      },
      error: () => { this.cancellingId = null; }
    });
  }

  getVehicleIcon(type: string): string {
    switch (type?.toUpperCase()) {
      case 'MOTORCYCLE': return 'two_wheeler';
      case 'TRUCK': return 'local_shipping';
      default: return 'directions_car';
    }
  }

  getNotificationIcon(type: string): string {
    switch (type?.toUpperCase()) {
      case 'BOOKING_CONFIRMED': return 'check_circle';
      case 'BOOKING_CANCELLED': return 'cancel';
      case 'PAYMENT_SUCCESS': return 'payments';
      case 'REMINDER': return 'schedule';
      default: return 'notifications';
    }
  }

  timeAgo(dateStr: string | undefined): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return '';
    const diff = Math.floor((Date.now() - date.getTime()) / 1000);
    if (diff < 60)   return 'Just now';
    if (diff < 3600) return Math.floor(diff / 60) + ' min ago';
    if (diff < 86400) return Math.floor(diff / 3600) + ' hr ago';
    if (diff < 604800) return Math.floor(diff / 86400) + ' day' + (Math.floor(diff / 86400) > 1 ? 's' : '') + ' ago';
    return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  }
}
