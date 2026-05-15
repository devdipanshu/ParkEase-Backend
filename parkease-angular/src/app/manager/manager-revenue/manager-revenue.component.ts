import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf, DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ParkingLotService } from '../../core/services/parking-lot.service';
import { PaymentService } from '../../core/services/payment.service';
import { AuthService } from '../../core/services/auth.service';
import { ParkingLot } from '../../shared/models/parking-lot.model';
import { forkJoin } from 'rxjs';

interface LotRevenue { lot: ParkingLot; revenue: number; }

@Component({
  selector: 'app-manager-revenue',
  standalone: true,
  imports: [NgFor, NgIf, DecimalPipe, MatIconModule],
  templateUrl: './manager-revenue.component.html'
})
export class ManagerRevenueComponent implements OnInit {
  lotRevenues: LotRevenue[] = [];
  loading = true;
  error = '';

  constructor(
    private lotService: ParkingLotService,
    private paymentService: PaymentService,
    private authService: AuthService
  ) {}

  ngOnInit() { this.loadData(); }

  loadData() {
    this.loading = true;
    this.error = '';
    const managerId = this.authService.getUserId();
    this.lotService.getLotsByManager(managerId).subscribe({
      next: (lots) => {
        if (!lots.length) { this.loading = false; return; }
        const revenueRequests = lots.map(lot =>
          this.paymentService.getRevenueByLot(lot.lotId!)
        );
        forkJoin(revenueRequests).subscribe({
          next: (revenues) => {
            this.lotRevenues = lots.map((lot, i) => ({ lot, revenue: revenues[i] || 0 }));
            this.loading = false;
          },
          error: () => {
            this.lotRevenues = lots.map(lot => ({ lot, revenue: 0 }));
            this.loading = false;
          }
        });
      },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }

  get totalRevenue(): number {
    return this.lotRevenues.reduce((sum, lr) => sum + lr.revenue, 0);
  }
}
