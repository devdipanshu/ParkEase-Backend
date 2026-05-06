package com.parkease.auth.resource;

import com.parkease.auth.dto.ChangePasswordRequest;
import com.parkease.auth.dto.LoginRequest;
import com.parkease.auth.dto.LoginResponse;
import com.parkease.auth.dto.RegisterRequest;
import com.parkease.auth.entity.User;
import com.parkease.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Principal principal) {
        User user = authService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(Principal principal,
                                               @RequestBody RegisterRequest request) {
        User current = authService.getUserByEmail(principal.getName());
        User updated = authService.updateProfile(current.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        User current = authService.getUserByEmail(principal.getName());
        authService.changePassword(current.getUserId(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(Principal principal) {
        User current = authService.getUserByEmail(principal.getName());
        authService.deactivateAccount(current.getUserId());
        return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
    }

    // TODO: In production, restrict this to ADMIN role only
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateAccount(@PathVariable Long id) {
        authService.reactivateAccount(id);
        return ResponseEntity.ok().build();
    }
}
