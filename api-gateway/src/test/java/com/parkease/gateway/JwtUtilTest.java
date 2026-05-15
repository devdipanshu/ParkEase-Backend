package com.parkease.gateway;

import com.parkease.gateway.config.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "parkease-super-secret-key-for-testing-1234567890";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    private String buildToken(long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "DRIVER");
        return Jwts.builder()
                .claims(claims)
                .subject("user@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = buildToken(3600000L);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        String token = buildToken(-1000L);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void extractEmail_returnsSubject() {
        String token = buildToken(3600000L);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractRole_returnsRole() {
        String token = buildToken(3600000L);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("DRIVER");
    }

    @Test
    void isTokenExpired_notExpired_returnsFalse() {
        String token = buildToken(3600000L);
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }
}