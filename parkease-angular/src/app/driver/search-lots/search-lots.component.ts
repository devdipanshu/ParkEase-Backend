import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search-lots',
  standalone: true,
  imports: [FormsModule, NgIf, MatIconModule],
  templateUrl: './search-lots.component.html'
})
export class SearchLotsComponent {
  mode: 'nearby' | 'city' = 'nearby';

  // Nearby mode
  lat: number | null = null;
  lng: number | null = null;
  radius: number = 5;
  locationError = '';
  locating = false;

  // City mode
  cityQuery = '';
  cityError = '';
  citySearching = false;

  constructor(private router: Router) {}

  useMyLocation() {
    this.locating = true;
    this.locationError = '';
    if (!navigator.geolocation) {
      this.locationError = 'Geolocation not supported in this browser.';
      this.locating = false;
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.lat = pos.coords.latitude;
        this.lng = pos.coords.longitude;
        this.locating = false;
      },
      () => {
        this.locationError = 'Could not get location. Please enter manually.';
        this.locating = false;
      }
    );
  }

  onNearbySearch() {
    if (!this.lat || !this.lng) return;
    this.router.navigate(['/driver/nearby-lots'], {
      queryParams: { lat: this.lat, lng: this.lng, radius: this.radius }
    });
  }

  onCitySearch() {
    const city = this.cityQuery.trim();
    if (!city) { this.cityError = 'Please enter a city name.'; return; }

    this.cityError = '';
    this.citySearching = true;

    // Geocode city → lat/lng using OpenStreetMap Nominatim (free, no key needed)
    fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(city)}&format=json&limit=1`, {
      headers: { 'Accept-Language': 'en' }
    })
      .then(r => r.json())
      .then((results: any[]) => {
        this.citySearching = false;
        if (!results.length) {
          this.cityError = `"${city}" not found. Check the spelling and try again.`;
          return;
        }
        const { lat, lon } = results[0];
        this.router.navigate(['/driver/nearby-lots'], {
          queryParams: { lat, lng: lon, radius: 50, city }
        });
      })
      .catch(() => {
        this.citySearching = false;
        this.cityError = 'Geocoding failed. Check your internet and try again.';
      });
  }
}
