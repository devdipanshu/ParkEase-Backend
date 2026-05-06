package com.parkease.auth.service;

import com.parkease.auth.dto.ChangePasswordRequest;
import com.parkease.auth.dto.LoginRequest;
import com.parkease.auth.dto.LoginResponse;
import com.parkease.auth.dto.RegisterRequest;
import com.parkease.auth.entity.User;

public interface AuthService {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    String refreshToken(String token);

    User getUserByEmail(String email);

    User getUserById(Long userId);

    User updateProfile(Long userId, RegisterRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void deactivateAccount(Long userId);

    void reactivateAccount(Long userId);
}
