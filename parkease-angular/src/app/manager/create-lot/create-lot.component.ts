import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-create-lot',
  standalone: true,
  imports: [FormsModule, NgIf, MatIconModule],
  templateUrl: './create-lot.component.html'
})
export class CreateLotComponent {
  lot = {
    name: '', address: '', city: '',
    latitude: null as number | null,
    longitude: null as number | null,
    totalSpots: 0, openTime: '08:00', closeTime: '22:00', imageUrl: ''
  };
  locating = false;
  errorMsg = '';
  submitting = false;

  constructor(
    private lotService: ParkingLotService,
    private authService: AuthService,
    private router: Router
  ) {}

  useGPS() {
    this.locating = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.lot.latitude = pos.coords.latitude;
        this.lot.longitude = pos.coords.longitude;
        this.locating = false;
      },
      () => { this.errorMsg = 'Could not get GPS location.'; this.locating = false; }
    );
  }

  onSubmit() {
    if (!this.lot.latitude || !this.lot.longitude) {
      this.errorMsg = 'Please provide latitude and longitude.';
      return;
    }
    this.submitting = true;
    this.lotService.createLot({
      ...this.lot,
      latitude: this.lot.latitude,
      longitude: this.lot.longitude,
      managerId: this.authService.getUserId()
    }).subscribe({
      next: () => this.router.navigate(['/manager/dashboard']),
      error: () => { this.errorMsg = 'Failed to create lot.'; this.submitting = false; }
    });
  }
}
