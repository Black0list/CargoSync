package com.spring.logitrack.dto;

import com.spring.logitrack.entity.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Role role;
}
