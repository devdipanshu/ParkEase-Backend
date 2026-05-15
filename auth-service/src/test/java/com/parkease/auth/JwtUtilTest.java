package com.parkease.auth;

import com.parkease.auth.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "parkease-super-secret-key-for-testing-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("user@test.com", 1L, "DRIVER");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("user@test.com", 1L, "DRIVER");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtUtil.generateToken("user@test.com", 42L, "DRIVER");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("user@test.com", 1L, "MANAGER");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("MANAGER");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user@test.com", 1L, "DRIVER");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("this.is.not.a.valid.token")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("user@test.com", 1L, "DRIVER");
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }
}
