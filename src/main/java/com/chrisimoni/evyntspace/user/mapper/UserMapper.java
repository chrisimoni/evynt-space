package com.chrisimoni.evyntspace.user.mapper;

import com.chrisimoni.evyntspace.user.dto.request.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.dto.response.UserResponse;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.dto.request.UserCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", // Makes it a Spring component (singleton)
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Important for updates!
public interface UserMapper {
    User toModel(UserCreateRequest dto);
    User updateUserFromDto(UserUpdateRequest request, @MappingTarget User userToUpdate);
    UserResponse toResponseDto(User user);
}
