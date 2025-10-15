package com.surest.member.service;

import com.surest.member.dto.LoginRequest;
import com.surest.member.dto.LoginResponse;
import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.UserRepository;
import com.surest.member.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {


    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) throws BusinessServiceException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessServiceException("User not found", HttpStatus.NOT_FOUND));

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        String token = jwtUtil.generateToken(user.getUsername(), roleNames);
        log.info("User {} logged in successfully", user.getUsername());
        return new LoginResponse(token);
    }
}
