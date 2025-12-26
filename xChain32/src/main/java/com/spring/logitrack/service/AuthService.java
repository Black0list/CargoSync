package com.spring.logitrack.service;

import com.spring.logitrack.dto.auth.JwtAuthResponse;
import com.spring.logitrack.dto.auth.RefreshTokenRequest;
import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;

public interface AuthService {
    JwtAuthResponse login(UserLoginDTO loginDto);
    JwtAuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
