import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { PaymentService } from '../../core/services/payment.service';
import { AuthService } from '../../core/services/auth.service';
import { Payment } from '../../shared/models/payment.model';

@Component({
  selector: 'app-payment-history',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, MatIconModule],
  templateUrl: './payment-history.component.html'
})
export class PaymentHistoryComponent implements OnInit {
  payments: Payment[] = [];
  loading = true;
  error = '';
  downloadingId: number | null = null;
  downloadError = '';

  constructor(
    private paymentService: PaymentService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadData();
    // Retry once after 3s — handles gateway cache miss after service restart
    setTimeout(() => { if (this.error) this.loadData(); }, 3000);
  }

  loadData() {
    this.loading = true;
    this.error = '';
    const userId = this.authService.getUserId();
    this.paymentService.getMyPayments(userId).subscribe({
      next: (data) => {
        this.payments = data.sort((a, b) => {
          const ta = a.paidAt ? new Date(a.paidAt).getTime() : (a.paymentId ?? 0);
          const tb = b.paidAt ? new Date(b.paidAt).getTime() : (b.paymentId ?? 0);
          return tb - ta;
        });
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }

  downloadReceipt(paymentId: number, transactionId: string) {
    this.downloadingId = paymentId;
    this.downloadError = '';
    this.paymentService.downloadReceipt(paymentId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `receipt_${transactionId || paymentId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.downloadingId = null;
      },
      error: () => {
        this.downloadError = 'Failed to download receipt. Please try again.';
        this.downloadingId = null;
      }
    });
  }
}
