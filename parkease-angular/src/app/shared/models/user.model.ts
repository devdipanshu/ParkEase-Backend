export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  fullName: string;
  email: string;
  role: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phone: string;
  role: string;
  vehiclePlate?: string;
  profilePicUrl?: string;
}

export interface UserProfile {
  userId: number;
  fullName: string;
  email: string;
  phone: string;
  role: string;
  vehiclePlate?: string;
  profilePicUrl?: string;
  isActive?: boolean;
  createdAt?: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  email: string;
  phone: string;
  vehiclePlate?: string;
  profilePicUrl?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
