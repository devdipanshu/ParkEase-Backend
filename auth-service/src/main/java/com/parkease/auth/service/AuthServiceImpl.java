package com.parkease.auth.service;

import com.parkease.auth.config.JwtUtil;
import com.parkease.auth.dto.ChangePasswordRequest;
import com.parkease.auth.dto.LoginRequest;
import com.parkease.auth.dto.LoginResponse;
import com.parkease.auth.dto.RegisterRequest;
import com.parkease.auth.entity.User;
import com.parkease.auth.exception.AccountDeactivatedException;
import com.parkease.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setVehiclePlate(request.getVehiclePlate());
        user.setProfilePicUrl(request.getProfilePicUrl());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new AccountDeactivatedException(
                    "Your account has been deactivated. Please contact support@parkease.com to reactivate.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole().name());
        return new LoginResponse(token, user.getUserId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    @Override
    public void logout(String token) {
        long ttl = jwtUtil.getExpirationMillis(token);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", ttl, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
                return false;
            }
            if (!jwtUtil.isTokenValid(token)) {
                return false;
            }
            String email = jwtUtil.extractEmail(token);
            return userRepository.findByEmail(email)
                    .map(User::getIsActive)
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String refreshToken(String token) {
        if (!validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }
        String email = jwtUtil.extractEmail(token);
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        logout(token);
        return jwtUtil.generateToken(email, userId, role);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, RegisterRequest request) {
        User user = getUserById(userId);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getVehiclePlate() != null) user.setVehiclePlate(request.getVehiclePlate());
        if (request.getProfilePicUrl() != null) user.setProfilePicUrl(request.getProfilePicUrl());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }
}
