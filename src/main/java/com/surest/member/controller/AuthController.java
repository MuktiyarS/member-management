package com.surest.member.controller;

import com.surest.member.dto.LoginRequest;
import com.surest.member.dto.LoginResponse;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws BusinessServiceException {
        return authService.login(request);
    }
}
