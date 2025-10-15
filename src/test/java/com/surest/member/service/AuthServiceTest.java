package com.surest.member.service;

import com.surest.member.dto.LoginRequest;
import com.surest.member.dto.LoginResponse;
import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.UserRepository;
import com.surest.member.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_login_ShouldReturnToken_WhenCredentialsValid() throws Exception {
        // given
        LoginRequest request = new LoginRequest("john", "password");
        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .passwordHash("hashed_pw")
                .email("john@example.com")
                .roles(Set.of(role))
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq("john"), anyList())).thenReturn("jwt-token-123");

        // no exception when authenticating
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("john", "password"));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("john", List.of("ROLE_USER"));
    }

    @Test
    void test_login_ShouldThrow_WhenAuthenticationFails() {
        // given
        LoginRequest request = new LoginRequest("john", "wrongpass");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");
    }

    @Test
    void test_login_ShouldThrow_WhenUserNotFound() {
        // given
        LoginRequest request = new LoginRequest("unknown", "pass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("User not found");
    }
}