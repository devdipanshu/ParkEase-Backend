import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SpotService } from '../../core/services/spot.service';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { Spot } from '../../shared/models/spot.model';

@Component({
  selector: 'app-manage-spots',
  standalone: true,
  imports: [NgFor, NgIf, FormsModule, MatIconModule],
  templateUrl: './manage-spots.component.html'
})
export class ManageSpotsComponent implements OnInit {
  lotId = 0;
  spots: Spot[] = [];
  loading = true;
  successMsg = '';
  errorMsg = '';

  bulk = {
    spotNumbers: '',
    floor: 1,
    spotType: 'STANDARD',
    vehicleType: 'FOUR_WHEELER',
    pricePerHour: 50
  };

  editingSpotId: number | null = null;
  editSpotForm: Partial<Spot> = {};

  constructor(
    private route: ActivatedRoute,
    private spotService: SpotService,
    private lotService: ParkingLotService
  ) {}

  ngOnInit() {
    this.lotId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadSpots();
  }

  loadSpots() {
    this.loading = true;
    this.spotService.getSpotsByLot(this.lotId).subscribe({
      next: (data) => { this.spots = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  addBulkSpots() {
    this.errorMsg = '';
    this.successMsg = '';
    const numbers = this.bulk.spotNumbers.split(',').map(s => s.trim()).filter(s => s);
    if (!numbers.length) { this.errorMsg = 'Enter at least one spot number.'; return; }

    const spotsToAdd: Spot[] = numbers.map(n => ({
      lotId: this.lotId,
      spotNumber: n,
      floor: this.bulk.floor,
      spotType: this.bulk.spotType,
      vehicleType: this.bulk.vehicleType,
      pricePerHour: this.bulk.pricePerHour
    }));

    this.spotService.addBulkSpots({ spots: spotsToAdd }).subscribe({
      next: () => {
        this.bulk.spotNumbers = '';
        this.spotService.getSpotsByLot(this.lotId).subscribe({
          next: (allSpots) => {
            this.spots = allSpots;
            this.successMsg = `${numbers.length} spot(s) added! Lot total: ${allSpots.length} spots.`;
            this.lotService.syncSpotCount(this.lotId, allSpots.length).subscribe({ error: () => {} });
          },
          error: () => { this.successMsg = `${numbers.length} spot(s) added!`; }
        });
      },
      error: () => { this.errorMsg = 'Failed to add spots.'; }
    });
  }

  startEditSpot(spot: Spot) {
    this.editingSpotId = spot.spotId!;
    this.editSpotForm = {
      spotNumber: spot.spotNumber,
      floor: spot.floor,
      spotType: spot.spotType,
      vehicleType: spot.vehicleType,
      pricePerHour: spot.pricePerHour,
      isEVCharging: spot.isEVCharging,
      isHandicapped: spot.isHandicapped
    };
  }

  saveEditSpot(spot: Spot) {
    this.successMsg = '';
    this.errorMsg = '';
    const payload: Partial<Spot> = { ...this.editSpotForm, lotId: this.lotId };
    this.spotService.updateSpot(spot.spotId!, payload).subscribe({
      next: (updated) => {
        const idx = this.spots.findIndex(s => s.spotId === spot.spotId);
        if (idx >= 0) this.spots[idx] = updated;
        this.editingSpotId = null;
        this.successMsg = `Spot ${updated.spotNumber} updated.`;
      },
      error: () => { this.errorMsg = 'Failed to update spot.'; }
    });
  }

  cancelEditSpot() {
    this.editingSpotId = null;
    this.editSpotForm = {};
  }

  deleteSpot(spot: Spot) {
    if (!confirm(`Delete spot ${spot.spotNumber}? This cannot be undone.`)) return;
    this.successMsg = '';
    this.errorMsg = '';
    this.spotService.deleteSpot(spot.spotId!).subscribe({
      next: () => {
        this.spots = this.spots.filter(s => s.spotId !== spot.spotId);
        this.successMsg = `Spot ${spot.spotNumber} deleted.`;
        this.lotService.syncSpotCount(this.lotId, this.spots.length).subscribe({ error: () => {} });
      },
      error: () => { this.errorMsg = 'Cannot delete spot. It may have active bookings.'; }
    });
  }
}
