import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [NgIf, RouterLink, MatIconModule],
  templateUrl: './footer.component.html'
})
export class FooterComponent {
  year = new Date().getFullYear();

  constructor(private authService: AuthService) {}

  get isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
  get role(): string { return this.authService.getRole() || ''; }
  get isDriver(): boolean { return this.role === 'DRIVER'; }
  get isManager(): boolean { return this.role === 'MANAGER'; }
}
