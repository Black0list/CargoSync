package com.spring.logitrack;

import com.spring.logitrack.dto.auth.JwtAuthResponse;
import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndLoginUserSuccessfully() {

        UserCreateDTO registerDto = new UserCreateDTO();
        registerDto.setEmail("test@example.com");
        registerDto.setName("Test User");
        registerDto.setPassword("password123");
        registerDto.setRole(Role.CLIENT);

        ResponseEntity<Void> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        registerDto,
                        Void.class
                );

        assertThat(registerResponse.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        // Verify user persisted
        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123"); // encoded
        assertThat(savedUser.isActive()).isTrue();


        UserLoginDTO loginDto = new UserLoginDTO();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        ResponseEntity<JwtAuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        loginDto,
                        JwtAuthResponse.class
                );


        assertThat(loginResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getAccessToken()).isNotBlank();
        assertThat(loginResponse.getBody().getRefreshToken()).isNotBlank();
    }
}
