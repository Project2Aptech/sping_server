package org.example.spring_server.mapper;

import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO.UserDetailResponse toDetailResponse(User user);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "accountType",  ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    User toEntity(UserDTO.RegisterRequest request);
}