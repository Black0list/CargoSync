package com.spring.logitrack.service;

import com.spring.logitrack.dto.UserLoginDTO;
import com.spring.logitrack.dto.UserRegisterDTO;
import com.spring.logitrack.dto.UserResponseDTO;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.mapper.UserMapper;
import com.spring.logitrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;

    @Autowired
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserResponseDTO register(UserRegisterDTO dto) {
        if (repo.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already in use");

        User user = UserMapper.toEntity(dto);
        user.setPassword(dto.getPassword());
        user.setActive(true);
        user.setRole(Role.CLIENT);
        return UserMapper.toResponse(repo.save(user));
    }

    public UserResponseDTO login(UserLoginDTO dto) {
        User user = repo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!dto.getPassword().equals(user.getPassword()))
            throw new IllegalArgumentException("Invalid email or password");

        return UserMapper.toResponse(user);
    }
}
