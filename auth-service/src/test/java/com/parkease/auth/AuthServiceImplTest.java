package com.parkease.auth;

import com.parkease.auth.config.JwtUtil;
import com.parkease.auth.dto.ChangePasswordRequest;
import com.parkease.auth.dto.LoginRequest;
import com.parkease.auth.dto.LoginResponse;
import com.parkease.auth.dto.RegisterRequest;
import com.parkease.auth.entity.User;
import com.parkease.auth.exception.AccountDeactivatedException;
import com.parkease.auth.repository.UserRepository;
import com.parkease.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(1L);
        activeUser.setEmail("user@test.com");
        activeUser.setFullName("Test User");
        activeUser.setPasswordHash("hashed_password");
        activeUser.setRole(User.Role.DRIVER);
        activeUser.setIsActive(true);
    }

    @Test
    void register_newEmail_savesAndReturnsUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setFullName("New User");
        request.setPassword("password123");
        request.setRole(User.Role.DRIVER);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setRole(User.Role.DRIVER);

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("user@test.com", 1L, "DRIVER")).thenReturn("jwt.token.here");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_deactivatedAccount_throwsException() {
        activeUser.setIsActive(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountDeactivatedException.class);
    }

    @Test
    void changePassword_correctCurrentPassword_updatesHash() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("oldPass", "hashed_password")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        authService.changePassword(1L, request);

        assertThat(activeUser.getPasswordHash()).isEqualTo("new_hash");
        verify(userRepository).save(activeUser);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBadCredentials() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newPass");

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deactivateAccount_setsIsActiveFalse() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        authService.deactivateAccount(1L);

        assertThat(activeUser.getIsActive()).isFalse();
    }
}
