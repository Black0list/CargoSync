package com.spring.logitrack.service;

import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.mapper.UserMapper;
import com.spring.logitrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService service;

    // ------------------------------------------------------------------
    // REGISTER
    // ------------------------------------------------------------------
    @Test
    void register_success() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("User A");
        dto.setEmail("user@test.com");
        dto.setPassword("pass");

        User entity = new User();
        entity.setId(1L);
        entity.setEmail("user@test.com");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail("user@test.com");
        saved.setPassword("pass");
        saved.setRole(Role.CLIENT);
        saved.setActive(true);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setEmail("user@test.com");

        when(repo.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponseDTO result = service.register(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repo).save(entity);
    }

    @Test
    void register_emailAlreadyUsed() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("exists@test.com");

        when(repo.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");
    }

    // ------------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------------
    @Test
    void login_success() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("u@test.com");
        dto.setPassword("1234");

        User user = new User();
        user.setId(2L);
        user.setEmail("u@test.com");
        user.setPassword("1234");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(2L);
        response.setEmail("u@test.com");

        when(repo.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = service.login(dto);

        assertThat(result.getEmail()).isEqualTo("u@test.com");
    }

    @Test
    void login_invalidEmail() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("wrong@test.com");
        dto.setPassword("1234");

        when(repo.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_invalidPassword() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("wrong");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("correct");

        when(repo.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------
    @Test
    void update_success() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("New Name");
        dto.setEmail("new@test.com");

        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.com");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail("new@test.com");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setEmail("new@test.com");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail(dto.getEmail())).thenReturn(false);

        // Patch simulate
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            UserCreateDTO d = inv.getArgument(1);
            u.setEmail(d.getEmail());
            u.setName(d.getName());
            return null;
        }).when(userMapper).patch(existing, dto);

        when(repo.save(existing)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponseDTO result = service.update(1L, dto);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void update_userNotFound() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("x@test.com");

        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void update_emailAlreadyExists() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("conflict@test.com");

        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.com");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    // ------------------------------------------------------------------
    // LIST
    // ------------------------------------------------------------------
    @Test
    void list_success() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@test.com");

        UserResponseDTO d = new UserResponseDTO();
        d.setId(1L);
        d.setEmail("a@test.com");

        when(repo.findAll()).thenReturn(List.of(u));
        when(userMapper.toResponse(u)).thenReturn(d);

        List<UserResponseDTO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("a@test.com");
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------
    @Test
    void get_success() {
        User u = new User();
        u.setId(5L);
        u.setEmail("get@test.com");

        UserResponseDTO d = new UserResponseDTO();
        d.setId(5L);
        d.setEmail("get@test.com");

        when(repo.findById(5L)).thenReturn(Optional.of(u));
        when(userMapper.toResponse(u)).thenReturn(d);

        UserResponseDTO result = service.get(5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void get_notFound() {
        when(repo.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(44L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------
    @Test
    void delete_hardDelete() {
        User u = new User();
        u.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(u));

        service.delete(1L, true);

        verify(repo).delete(u);
    }

    @Test
    void delete_softDelete() {
        User u = new User();
        u.setId(1L);
        u.setActive(true);

        when(repo.findById(1L)).thenReturn(Optional.of(u));

        service.delete(1L, false);

        assertThat(u.isActive()).isFalse();
        verify(repo).save(u);
    }

    @Test
    void delete_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }
}
