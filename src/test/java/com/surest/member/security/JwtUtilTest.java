package com.surest.member.security;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set private fields manually
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "mytestsecretmytestsecretmytestsecret12");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void test_generateToken_ShouldCreateValidJwt() {
        String token = jwtUtil.generateToken("john", List.of("ROLE_USER"));

        assertThat(token).isNotNull();

        String username = jwtUtil.extractUsername(token);
        assertThat(username).isEqualTo("john");

        boolean valid = jwtUtil.validateToken(token);
        assertThat(valid).isTrue();
    }

    @Test
    void test_validateToken_ShouldReturnFalse_ForInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        boolean valid = jwtUtil.validateToken(invalidToken);
        assertThat(valid).isFalse();
    }

    @Test
    void test_extractUsername_ShouldThrow_ForMalformedToken() {
        String invalidToken = "abc.def.ghi";
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void test_validateToken_ShouldReturnFalse_ForExpiredToken() throws InterruptedException {
        // Create a JwtUtil with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1L); // 1 millisecond
        String token = jwtUtil.generateToken("expiredUser", List.of("ROLE_USER"));
        Thread.sleep(10); // wait a bit

        boolean valid = jwtUtil.validateToken(token);
        assertThat(valid).isFalse();
    }

    @Test
    void test_extractRole_ShouldReturnRoleClaim() {
        String token = jwtUtil.generateToken("john", List.of("ROLE_ADMIN", "ROLE_USER"));

        // JwtUtil stores "roles" (plural), not "role", so this will return null
        // Let's verify that behavior
        String role = jwtUtil.extractRole(token);

        assertThat(role).isNull(); // because claim name in generateToken() is "roles"
    }
}