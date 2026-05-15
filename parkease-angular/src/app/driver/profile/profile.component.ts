import { Component, OnInit } from '@angular/core';
import { NgIf, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { UserProfile, UpdateProfileRequest, ChangePasswordRequest } from '../../shared/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [NgIf, SlicePipe, FormsModule, MatIconModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  loading = true;
  error = '';

  editMode = false;
  editForm: UpdateProfileRequest = { fullName: '', email: '', phone: '', vehiclePlate: '', profilePicUrl: '' };
  saveLoading = false;
  saveSuccess = '';
  saveError = '';

  showPasswordForm = false;
  passwordForm: ChangePasswordRequest = { currentPassword: '', newPassword: '' };
  confirmPassword = '';
  passwordLoading = false;
  passwordSuccess = '';
  passwordError = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.authService.getProfile().subscribe({
      next: (data) => { this.profile = data; this.loading = false; },
      error: () => { this.error = 'Failed to load profile.'; this.loading = false; }
    });
  }

  startEdit() {
    if (!this.profile) return;
    this.editForm = {
      fullName: this.profile.fullName,
      email: this.profile.email,
      phone: this.profile.phone || '',
      vehiclePlate: this.profile.vehiclePlate || '',
      profilePicUrl: this.profile.profilePicUrl || ''
    };
    this.editMode = true;
    this.saveSuccess = '';
    this.saveError = '';
  }

  saveProfile() {
    this.saveLoading = true;
    this.saveError = '';
    this.authService.updateProfile(this.editForm).subscribe({
      next: (data) => {
        this.profile = data;
        this.editMode = false;
        this.saveLoading = false;
        this.saveSuccess = 'Profile updated successfully!';
        localStorage.setItem('fullName', data.fullName);
      },
      error: () => {
        this.saveError = 'Update failed. Please try again.';
        this.saveLoading = false;
      }
    });
  }

  changePassword() {
    this.passwordError = '';
    if (this.passwordForm.newPassword !== this.confirmPassword) {
      this.passwordError = 'Passwords do not match.';
      return;
    }
    if (this.passwordForm.newPassword.length < 6) {
      this.passwordError = 'Password must be at least 6 characters.';
      return;
    }
    this.passwordLoading = true;
    this.authService.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.passwordSuccess = 'Password changed successfully!';
        this.passwordForm = { currentPassword: '', newPassword: '' };
        this.confirmPassword = '';
        this.showPasswordForm = false;
        this.passwordLoading = false;
      },
      error: () => {
        this.passwordError = 'Incorrect current password or server error.';
        this.passwordLoading = false;
      }
    });
  }
}
