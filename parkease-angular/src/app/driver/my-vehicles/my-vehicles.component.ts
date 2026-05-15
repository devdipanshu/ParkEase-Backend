import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { VehicleService } from '../../core/services/vehicle.service';
import { AuthService } from '../../core/services/auth.service';
import { Vehicle } from '../../shared/models/vehicle.model';

@Component({
  selector: 'app-my-vehicles',
  standalone: true,
  imports: [NgFor, NgIf, SlicePipe, FormsModule, MatIconModule],
  templateUrl: './my-vehicles.component.html'
})
export class MyVehiclesComponent implements OnInit {
  vehicles: Vehicle[] = [];
  loading = true;
  showForm = false;
  successMsg = '';
  errorMsg = '';

  newVehicle: Vehicle = {
    ownerId: 0, licensePlate: '', make: '', model: '',
    color: '', vehicleType: 'CAR', isEV: false
  };

  editingId: number | null = null;
  editForm: Partial<Vehicle> = {};

  constructor(
    private vehicleService: VehicleService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.newVehicle.ownerId = this.authService.getUserId();
    this.loadVehicles();
  }

  loadVehicles() {
    this.vehicleService.getMyVehicles(this.authService.getUserId()).subscribe({
      next: (data) => { this.vehicles = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  addVehicle() {
    this.errorMsg = '';
    this.vehicleService.addVehicle(this.newVehicle).subscribe({
      next: () => {
        this.successMsg = 'Vehicle added!';
        this.showForm = false;
        this.loadVehicles();
        this.resetForm();
      },
      error: () => { this.errorMsg = 'Failed to add vehicle. Plate may already exist.'; }
    });
  }

  startEdit(v: Vehicle) {
    this.editingId = v.vehicleId!;
    this.editForm = { make: v.make, model: v.model, color: v.color, vehicleType: v.vehicleType, isEV: v.isEV };
    this.errorMsg = '';
  }

  saveEdit(v: Vehicle) {
    this.vehicleService.updateVehicle(v.vehicleId!, this.editForm).subscribe({
      next: () => {
        this.successMsg = 'Vehicle updated!';
        this.editingId = null;
        this.loadVehicles();
      },
      error: () => { this.errorMsg = 'Update failed.'; }
    });
  }

  cancelEdit() {
    this.editingId = null;
    this.editForm = {};
  }

  deleteVehicle(id: number) {
    if (!confirm('Remove this vehicle?')) return;
    this.vehicleService.deleteVehicle(id).subscribe({
      next: () => { this.successMsg = 'Vehicle removed.'; this.loadVehicles(); },
      error: () => { this.errorMsg = 'Delete failed.'; }
    });
  }

  private resetForm() {
    this.newVehicle = {
      ownerId: this.authService.getUserId(),
      licensePlate: '', make: '', model: '',
      color: '', vehicleType: 'CAR', isEV: false
    };
  }
}
