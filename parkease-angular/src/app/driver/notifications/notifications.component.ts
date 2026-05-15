import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { Notification } from '../../shared/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, MatIconModule],
  templateUrl: './notifications.component.html'
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading = true;
  error = '';

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const userId = this.authService.getUserId();
    this.notificationService.markAllAsRead(userId).subscribe({ error: () => {} });
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = '';
    const userId = this.authService.getUserId();
    this.notificationService.getMyNotifications(userId).subscribe({
      next: (data) => { this.notifications = data; this.loading = false; },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }
}
