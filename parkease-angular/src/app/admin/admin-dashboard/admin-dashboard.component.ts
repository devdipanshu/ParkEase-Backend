import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgIf, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [NgIf, RouterLink, MatIconModule, DatePipe],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  summary: any = null;
  loading = true;
  error = '';
  lastUpdated: Date | null = null;
  private refreshInterval: any;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadData();
    this.refreshInterval = setInterval(() => this.loadData(), 30000);
  }

  ngOnDestroy() {
    clearInterval(this.refreshInterval);
  }

  loadData() {
    this.loading = true;
    this.error = '';
    this.http.get(`${environment.apiUrl}/api/analytics/summary`).subscribe({
      next: (data) => { this.summary = data; this.loading = false; this.lastUpdated = new Date(); },
      error: () => { this.error = 'Failed to load. Please try again.'; this.loading = false; }
    });
  }
}
