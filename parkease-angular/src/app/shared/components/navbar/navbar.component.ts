import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NgIf, AsyncPipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, NgIf, AsyncPipe, MatIconModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {
  displayName: string | null = null;
  unreadCount: number = 0;

  loggedIn$!: ReturnType<AuthService['loggedIn$']['pipe']>;
  role$!: ReturnType<AuthService['role$']['pipe']>;

  constructor(
    public authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loggedIn$ = this.authService.loggedIn$;
    this.role$ = this.authService.role$;
    this.displayName = this.authService.getFullName() || this.authService.getEmail();

    if (this.authService.isLoggedIn()) {
      const userId = this.authService.getUserId();
      this.notificationService.getUnreadCount(userId).subscribe({
        next: (res) => this.unreadCount = res.unreadCount,
        error: () => {}
      });
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
