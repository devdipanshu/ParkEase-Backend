import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest, LoginResponse, RegisterRequest,
  UserProfile, UpdateProfileRequest, ChangePasswordRequest
} from '../../shared/models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;

  private loggedInSubject = new BehaviorSubject<boolean>(this.isLoggedIn());
  loggedIn$ = this.loggedInSubject.asObservable();

  private roleSubject = new BehaviorSubject<string | null>(this.getRole());
  role$ = this.roleSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/api/auth/login`, request);
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/auth/register`, request);
  }

  saveSession(response: LoginResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('userId', response.userId.toString());
    localStorage.setItem('role', response.role);
    localStorage.setItem('email', response.email);
    localStorage.setItem('fullName', response.fullName || '');

    this.loggedInSubject.next(true);
    this.roleSubject.next(response.role);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/api/auth/profile`);
  }

  updateProfile(req: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/api/auth/profile`, req);
  }

  changePassword(req: ChangePasswordRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/auth/password`, req);
  }

  deactivateAccount(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/api/auth/deactivate`);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserId(): number {
    return Number(localStorage.getItem('userId'));
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getFullName(): string | null {
    return localStorage.getItem('fullName');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  logout(): void {
    localStorage.clear();
    this.loggedInSubject.next(false);
    this.roleSubject.next(null);
  }
}
