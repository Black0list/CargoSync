package com.spring.logitrack.dto.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private String role;
}
