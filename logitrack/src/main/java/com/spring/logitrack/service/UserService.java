package com.spring.logitrack.service;

import com.spring.logitrack.dto.user.UserLoginDTO;
import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.mapper.UserMapper;
import com.spring.logitrack.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final UserMapper userMapper;

    public UserResponseDTO register(UserCreateDTO dto) {
        if (repo.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already in use");

        User user = userMapper.toEntity(dto);
        user.setPassword(dto.getPassword());
        user.setActive(true);
        user.setRole(Role.CLIENT);
        return userMapper.toResponse(repo.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO login(UserLoginDTO dto) {
        User user = repo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!dto.getPassword().equals(user.getPassword()))
            throw new IllegalArgumentException("Invalid email or password");

        return userMapper.toResponse(user);
    }

    public UserResponseDTO update(Long id, UserCreateDTO dto) {
        User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(u.getEmail())
                && repo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        userMapper.patch(u, dto);
        u = repo.save(u);
        return userMapper.toResponse(u);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() {
        List<User> users = repo.findAll();
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO get(Long id) {
        User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toResponse(u);
    }

    public void delete(Long id, boolean hard) {
        User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (hard) {
            repo.delete(u);
        } else {
            u.setActive(false);
            repo.save(u);
        }
    }
}
