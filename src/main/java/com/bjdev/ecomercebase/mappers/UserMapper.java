package com.bjdev.ecomercebase.mappers;

import com.bjdev.ecomercebase.dto.response.UserResponse;
import com.bjdev.ecomercebase.models.user.Role;
import com.bjdev.ecomercebase.models.user.User;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    default List<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
