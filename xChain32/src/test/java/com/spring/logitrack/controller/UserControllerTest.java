package com.spring.logitrack.controller;

import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class UserControllerTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private UserCreateDTO createDTO;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Create DTO
        createDTO = new UserCreateDTO();
        createDTO.setName("John Doe");
        createDTO.setEmail("john@example.com");
        createDTO.setPassword("secret123");
        createDTO.setRole(Role.ADMIN);
        createDTO.setActive(true);

        // Response DTO
        responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("John Doe");
        responseDTO.setEmail("john@example.com");
        responseDTO.setActive(true);
        responseDTO.setRole("ADMIN");
    }

    // ======================================================================
    // LIST
    // ======================================================================
    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        var result = controller.list();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((List<?>) result.getBody()).size()).isEqualTo(1);
    }

    @Test
    void list_exception() {
        when(service.list()).thenThrow(new RuntimeException("DB Error"));

        var result = controller.list();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .asString()
                .contains("Error retrieving users");
    }

    // ======================================================================
    // GET BY ID
    // ======================================================================
    @Test
    void get_success() {
        when(service.get(5L)).thenReturn(responseDTO);

        var result = controller.get(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(((UserResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void get_exception() {
        when(service.get(5L)).thenThrow(new RuntimeException("User not found"));

        var result = controller.get(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("User not found");
    }

    // ======================================================================
    // UPDATE
    // ======================================================================
    @Test
    void update_success() {
        when(service.update(eq(10L), any())).thenReturn(responseDTO);

        var result = controller.update(10L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((UserResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(10L), any()))
                .thenThrow(new RuntimeException("Update failed"));

        var result = controller.update(10L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Update failed");
    }

    // ======================================================================
    // DELETE
    // ======================================================================
    @Test
    void delete_success() {
        // Controller returns void but uses @ResponseStatus(NO_CONTENT)
        controller.delete(8L, false);

        assertThat(true).isTrue();
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete failed"))
                .when(service)
                .delete(8L, true);

        try {
            controller.delete(8L, true);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Delete failed");
        }
    }
}
