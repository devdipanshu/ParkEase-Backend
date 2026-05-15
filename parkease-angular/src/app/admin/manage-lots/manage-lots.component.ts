import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { ParkingLot } from '../../shared/models/parking-lot.model';

@Component({
  selector: 'app-manage-lots',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink, MatIconModule],
  templateUrl: './manage-lots.component.html'
})
export class ManageLotsComponent implements OnInit {
  pendingLots: ParkingLot[] = [];
  approvedLots: ParkingLot[] = [];
  loading = true;
  error = '';
  successMsg = '';

  constructor(private lotService: ParkingLotService) {}

  ngOnInit() { this.loadData(); }

  loadData() {
    this.loading = true;
    this.error = '';
    this.successMsg = '';

    this.lotService.getPendingLots().subscribe({
      next: (data) => { this.pendingLots = data; },
      error: () => { this.pendingLots = []; }
    });

    this.lotService.getAllApprovedLots().subscribe({
      next: (data) => { this.approvedLots = data; this.loading = false; },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }

  approve(lotId: number) {
    this.lotService.approveLot(lotId).subscribe({
      next: () => { this.successMsg = `Lot #${lotId} approved!`; this.loadData(); },
      error: () => { this.successMsg = 'Approval failed.'; }
    });
  }

  deleteLot(lot: ParkingLot) {
    if (!confirm(`Delete lot "${lot.name}"? This cannot be undone.`)) return;
    this.successMsg = '';
    this.lotService.deleteLot(lot.lotId!).subscribe({
      next: () => {
        this.pendingLots = this.pendingLots.filter(l => l.lotId !== lot.lotId);
        this.approvedLots = this.approvedLots.filter(l => l.lotId !== lot.lotId);
        this.successMsg = `Lot "${lot.name}" deleted.`;
      },
      error: () => { this.successMsg = 'Delete failed. Lot may have active bookings.'; }
    });
  }
}
