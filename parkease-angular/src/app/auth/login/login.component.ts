import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf, MatIconModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  errorMsg = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.loading = true;
    this.errorMsg = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.authService.saveSession(res);
        if (res.role === 'DRIVER')       this.router.navigate(['/driver/dashboard']);
        else if (res.role === 'MANAGER') this.router.navigate(['/manager/dashboard']);
        else if (res.role === 'ADMIN')   this.router.navigate(['/admin/dashboard']);
      },
      error: () => {
        this.errorMsg = 'Invalid email or password. Please try again.';
        this.loading = false;
      }
    });
  }
}
