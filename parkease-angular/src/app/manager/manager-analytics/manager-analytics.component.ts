import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgFor, NgIf, DecimalPipe, DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-manager-analytics',
  standalone: true,
  imports: [NgFor, NgIf, DecimalPipe, MatIconModule, DatePipe],
  templateUrl: './manager-analytics.component.html'
})
export class ManagerAnalyticsComponent implements OnInit, OnDestroy {
  lotId = 0;
  occupancy: any = null;
  peakHours: any[] = [];
  loading = true;
  error = '';
  lastUpdated: Date | null = null;
  apiUrl = environment.apiUrl;
  private refreshInterval: any;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    this.lotId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData();
    this.refreshInterval = setInterval(() => this.loadData(), 30000);
  }

  ngOnDestroy() {
    clearInterval(this.refreshInterval);
  }

  loadData() {
    this.loading = true;
    this.error = '';

    this.http.get(`${this.apiUrl}/api/analytics/occupancy/${this.lotId}`).subscribe({
      next: (data) => { this.occupancy = data; },
      error: () => {}
    });

    this.http.get<any[]>(`${this.apiUrl}/api/analytics/peak-hours/${this.lotId}`).subscribe({
      next: (data) => { this.peakHours = data; this.loading = false; this.lastUpdated = new Date(); },
      error: () => { this.error = 'Failed to load analytics.'; this.loading = false; }
    });
  }

  get occupancyPercent(): number {
    return Math.min(Math.round(this.occupancy?.currentOccupancy || 0), 100);
  }
}
