package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.user.UserCreateDTO;
import com.spring.logitrack.dto.user.UserResponseDTO;
import com.spring.logitrack.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserCreateDTO dto);

    UserResponseDTO toResponse(User u);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget User user, UserCreateDTO dto);
}