package com.surest.member.security;


import com.surest.member.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    // Test: valid token → user authenticated
    @Test
    void test_doFilterInternal_ShouldAuthenticateUser_WhenValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String username = "john";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(true);

        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo(username);
        verify(filterChain).doFilter(request, response);
    }

    //  Test: missing Authorization header
    @Test
    void test_doFilterInternal_ShouldSkip_WhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, customUserDetailsService);
    }

    //  Test: missing Authorization header
    @Test
    void test_doFilterInternal_ShouldSkip_WhenInvalidAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(" InvalidHeader ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, customUserDetailsService);
    }

    //  Test: invalid token
    @Test
    void test_doFilterInternal_ShouldNotAuthenticate_WhenInvalidToken() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn("john");
        when(jwtUtil.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    //  Test: authentication already present → skip
    @Test
    void test_doFilterInternal_ShouldSkip_WhenAlreadyAuthenticated() throws ServletException, IOException {
        var existingAuth = new UsernamePasswordAuthenticationToken("existingUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("someUser");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).extractUsername("token"); // allowed
        verify(jwtUtil, never()).validateToken(anyString()); // should not go this far
        verifyNoInteractions(customUserDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}