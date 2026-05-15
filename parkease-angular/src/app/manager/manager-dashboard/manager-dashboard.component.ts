import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { AuthService } from '../../core/services/auth.service';
import { ParkingLot } from '../../shared/models/parking-lot.model';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink, FormsModule, MatIconModule],
  templateUrl: './manager-dashboard.component.html'
})
export class ManagerDashboardComponent implements OnInit, OnDestroy {
  lots: ParkingLot[] = [];
  loading = true;
  error = '';
  successMsg = '';
  private refreshInterval: any;

  showEditModal = false;
  editForm: Partial<ParkingLot> = {};
  editingLotId: number | null = null;
  editSubmitting = false;

  constructor(
    private lotService: ParkingLotService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadData();
    this.refreshInterval = setInterval(() => this.loadData(), 30000);
  }

  ngOnDestroy() {
    clearInterval(this.refreshInterval);
  }

  loadData() {
    if (!this.loading) this.loading = true;
    this.error = '';
    const managerId = this.authService.getUserId();
    this.lotService.getLotsByManager(managerId).subscribe({
      next: (data) => { this.lots = data; this.loading = false; },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }

  toggleOpen(lotId: number) {
    this.lotService.toggleOpen(lotId).subscribe({
      next: (updated) => {
        const lot = this.lots.find(l => l.lotId === lotId);
        if (lot) lot.isOpen = updated.isOpen;
      },
      error: () => {}
    });
  }

  startEdit(lot: ParkingLot) {
    this.editingLotId = lot.lotId!;
    this.editForm = {
      name: lot.name,
      address: lot.address,
      city: lot.city,
      latitude: lot.latitude,
      longitude: lot.longitude,
      openTime: lot.openTime,
      closeTime: lot.closeTime,
      totalSpots: lot.totalSpots,
      managerId: lot.managerId,
      isOpen: lot.isOpen
    };
    this.showEditModal = true;
  }

  saveEdit() {
    if (!this.editingLotId) return;
    this.editSubmitting = true;
    this.successMsg = '';
    this.lotService.updateLot(this.editingLotId, this.editForm).subscribe({
      next: (updated) => {
        const idx = this.lots.findIndex(l => l.lotId === this.editingLotId);
        if (idx >= 0) this.lots[idx] = updated;
        this.showEditModal = false;
        this.editSubmitting = false;
        this.successMsg = 'Lot updated successfully.';
      },
      error: () => {
        this.editSubmitting = false;
        this.successMsg = 'Update failed. Please try again.';
      }
    });
  }

  cancelEdit() {
    this.showEditModal = false;
    this.editingLotId = null;
    this.editForm = {};
  }

  deleteLot(lot: ParkingLot) {
    if (!confirm(`Delete lot "${lot.name}"? All spots will be removed. This cannot be undone.`)) return;
    this.successMsg = '';
    this.lotService.deleteLot(lot.lotId!).subscribe({
      next: () => {
        this.lots = this.lots.filter(l => l.lotId !== lot.lotId);
        this.successMsg = `Lot "${lot.name}" deleted.`;
      },
      error: () => { this.successMsg = 'Delete failed. Lot may have active bookings.'; }
    });
  }
}
