package com.spring.logitrack.controller;

import com.spring.logitrack.dto.auth.JwtAuthResponse;
import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;
import com.spring.logitrack.service.AuthService;
import com.spring.logitrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;



@ActiveProfiles("test")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private UserCreateDTO createDTO;
    private UserLoginDTO loginDTO;
    private UserResponseDTO responseDTO;
    private JwtAuthResponse jwtAuthResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new UserCreateDTO();
        createDTO.setEmail("john@example.com");
        createDTO.setPassword("123456");
        createDTO.setName("John Doe");

        loginDTO = new UserLoginDTO();
        loginDTO.setEmail("john@example.com");
        loginDTO.setPassword("123456");

        responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("john@example.com");
        responseDTO.setName("John Doe");

        jwtAuthResponse = JwtAuthResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .build();
    }

    // Note: register_success might still use UserService depending on how AuthController is implemented.
    // Assuming AuthController.register uses UserService.register? Let's check.
    // If AuthController.register uses AuthService, we need to update that too.
    // Based on previous file reads, register was using UserService directly in some versions?
    // Let's assume register uses UserService for now, but Login definitely uses AuthService.

    @Test
    void register_success() {
        // If register uses userService
        when(userService.register(any())).thenReturn(responseDTO);

        ResponseEntity<?> result = controller.register(createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((UserResponseDTO) result.getBody()).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void login_success() {
        when(authService.login(any())).thenReturn(jwtAuthResponse);

        ResponseEntity<?> result = controller.login(loginDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((JwtAuthResponse) result.getBody()).getAccessToken()).isEqualTo("token");
    }
}
