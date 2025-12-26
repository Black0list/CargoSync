package com.spring.logitrack.controller;

import com.spring.logitrack.dto.auth.JwtAuthResponse;
import com.spring.logitrack.dto.auth.RefreshTokenRequest;
import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.service.AuthService;
import com.spring.logitrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(dto));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody UserLoginDTO loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> loginOld(@Valid @RequestBody UserLoginDTO loginDto) {
         return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<JwtAuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }
}
