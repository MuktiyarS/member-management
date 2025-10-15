package com.surest.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.member.dto.LoginRequest;
import com.surest.member.dto.LoginResponse;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.security.JwtAuthenticationFilter;
import com.surest.member.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void test_login_ShouldReturnTokens_WhenCredentialsValid() throws Exception {
        // given
        LoginRequest request = new LoginRequest("admin2", "admin123");
        LoginResponse response = new LoginResponse("jwt-token-abc");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"));
    }

    @Test
    void test_login_ShouldReturn400_WhenValidationFails() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("john_doe", "");

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_login_ShouldReturn400_WhenUserNameBlank() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("", "gdg345");

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_login_ShouldReturn400_WhenBothBlank() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("  ", "  ");

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_login_ShouldReturn404_WhenUserNotFound() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("John", "gdg345");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_login_ShouldReturn401_WhenUserNotFound() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("John", "g454dg345");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_login_ShouldReturn401_InsufficientAuthentication() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("Johnee", "g454dg345");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InsufficientAuthenticationException("Insufficient Authentication"));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_login_ShouldReturnRuntimeException() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("Johnee", "g454dg345");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Runtime Exception"));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_login_ShouldReturnBusinessException() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("Johnee", "g454dg345");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessServiceException("Runtime Exception", HttpStatus.UNAUTHORIZED));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_login_ShouldReturnWhenWrongUrl() throws Exception {
        // given: missing password
        LoginRequest invalidRequest = new LoginRequest("Johnee", "g454dg345");
        when(authService.login(any(LoginRequest.class))).thenReturn(null);

        // when + then
        mockMvc.perform(post("/auth/login2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void test_login_ShouldReturn500_WhenServiceThrowsBusinessException() throws Exception {
        // given
        LoginRequest request = new LoginRequest("john_doe", "wrongPassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessServiceException("Invalid credentials"));

        // when + then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid credentials")));
    }
}