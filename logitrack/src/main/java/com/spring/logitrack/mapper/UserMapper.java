package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.UserRegisterDTO;
import com.spring.logitrack.dto.UserResponseDTO;
import com.spring.logitrack.entity.User;

public class UserMapper {

    public static User toEntity(UserRegisterDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public static UserResponseDTO toResponse(User user) {
        UserResponseDTO res = new UserResponseDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setActive(user.isActive());
        res.setRole(user.getRole().name());
        return res;
    }
}
