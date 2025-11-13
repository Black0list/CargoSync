package com.spring.logitrack.controller;

import com.spring.logitrack.dto.user.*;
import com.spring.logitrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController controller;

    private UserCreateDTO createDTO;
    private UserLoginDTO loginDTO;
    private UserResponseDTO responseDTO;

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
    }

    @Test
    void register_success() {
        when(userService.register(any())).thenReturn(responseDTO);

        ResponseEntity<?> result = controller.register(createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((UserResponseDTO) result.getBody()).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void login_success() {
        when(userService.login(any())).thenReturn(responseDTO);

        ResponseEntity<?> result = controller.login(loginDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((UserResponseDTO) result.getBody()).getName()).isEqualTo("John Doe");
    }
}
