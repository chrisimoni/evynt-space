package com.chrisimoni.evyntspace.user.mapper;

import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.dto.UserCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", // Makes it a Spring component (singleton)
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Important for updates!
public interface UserMapper {
    User toEnity(UserCreateRequest dto);
    UserResponse toResponseDto(User user);

    User updateUserFromDto(UserUpdateRequest request, @MappingTarget User userToUpdate);

    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    @Mapping(source = "last", target = "isLast")
    PageResponse<UserResponse> toPageResponse(Page<User> userPage);
}
