package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.dto.UserStatusUpdateRequest;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock all dependencies that UserController injects
    @MockBean
    private UserService userService;
    @MockBean
    private UserMapper userMapper;

    private static final String BASE_USERS_URL = "/api/v1/users";

    @Test
    @DisplayName("Should return a user successfully")
    void testGetUserById_shouldReturnAUser() throws Exception {
        // Arrange
        // Create mock User entity
        User user = mockUser();
        UserResponse userResponse = mockUserResponse(user);

        // Define mock behavior for service and mapper
        doReturn(user).when(userService).findById(eq(user.getId()));
        doReturn(userResponse).when(userMapper).toResponseDto(eq(user));

        // Act & Assert
        mockMvc.perform(get(BASE_USERS_URL + "/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User retrieved."))
                .andExpect(jsonPath("$.data.id").value(user.getId().toString())) // Convert UUID to string for comparison
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()));

        // Verify service and mapper interactions
        verify(userService, times(1)).findById(eq(user.getId()));
        verify(userMapper, times(1)).toResponseDto(eq(user));
    }

    @Test
    @DisplayName("Should return 404 Not Found if user does not exist")
    void testGetUserById_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();

        String errorMessage = "User not found with ID: " + nonExistentUserId;
        doThrow(new ResourceNotFoundException(errorMessage))
                .when(userService).findById(eq(nonExistentUserId));

        // Act & Assert
        mockMvc.perform(get(BASE_USERS_URL + "/{id}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expect 400 if BadRequestException maps to it
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        // Verify service was called, mapper should not be called
        verify(userService, times(1)).findById(eq(nonExistentUserId));
        verifyNoInteractions(userMapper); // Mapper should not be invoked if user not found
    }

    @Test
    @DisplayName("Should successfully update a user")
    void testUpdateUser_shouldUpdateUserSuccessfully() throws Exception {
        // Arrange
        User existingUser = mockUser();

        // Request DTO with fields to update
        UserUpdateRequest updateRequest = createUserUpdateRequestObject();
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        // Expected updated user
        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setFirstName(updateRequest.firstName());
        updatedUser.setLastName(updateRequest.lastName());
        updatedUser.setCompany(updateRequest.company());
        updatedUser.setPhoneNumber(updateRequest.phoneNumber());


        UserResponse expectedResponse = mockUserResponse(updatedUser);

        // Mock behavior:
        doReturn(existingUser).when(userService).findById(eq(existingUser.getId()));
        doReturn(updatedUser).when(userMapper).updateUserFromDto(eq(updateRequest), eq(existingUser));
        doReturn(updatedUser).when(userService).save(eq(updatedUser));
        doReturn(expectedResponse).when(userMapper).toResponseDto(eq(updatedUser));

        // Act & Assert
        mockMvc.perform(patch(BASE_USERS_URL + "/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User updated."))
                .andExpect(jsonPath("$.data.id").value(updatedUser.getId().toString()))
                .andExpect(jsonPath("$.data.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.data.firstName").value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(updatedUser.getLastName()));

        // Verify interactions
        verify(userService, times(1)).findById(eq(existingUser.getId()));
        verify(userMapper, times(1)).updateUserFromDto(eq(updateRequest), eq(existingUser));
        verify(userService, times(1)).save(eq(updatedUser));
        verify(userMapper, times(1)).toResponseDto(eq(updatedUser));
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testUpdateUser_shouldReturnUserNotFound() throws Exception {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        UserUpdateRequest updateRequest = createUserUpdateRequestObject();
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        String errorMessage= "User not found with ID: " + nonExistentUserId;
        doThrow(new ResourceNotFoundException(errorMessage))
                .when(userService).findById(eq(nonExistentUserId));

        // Act & Assert
        mockMvc.perform(patch(BASE_USERS_URL + "/{id}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Should successfully update user status")
    void testUpdateUserStatus_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);
        String requestJson = objectMapper.writeValueAsString(request);

        // Mock behavior: userService.updateStatus is a void method
        doNothing().when(userService).updateStatus(eq(userId), eq(request.status()));

        // Act & Assert
        mockMvc.perform(patch(BASE_USERS_URL + "/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User status updated"));

        // Verify interactions
        verify(userService, times(1)).updateStatus(eq(userId), eq(request.status()));
    }


    @Test
    @DisplayName("Should return a list of users successfully")
    void testGetUsers_WithoutAnyFilter_shouldReturnListOfUsers() throws Exception {
        // Arrange
        UserSearchCriteria filter = new UserSearchCriteria();

        // Create mock User entities
        User user1 = mockUser();
        User user2 = mockUser();
        user2.setEmail("jane.smith@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        List<User> mockUsers = List.of(user1, user2);

        // Create a mock Page of Users
        Page<User> usersPage = new PageImpl<>(mockUsers);
        when(userService.findAllUsers(any(UserSearchCriteria.class))).thenReturn(usersPage);

        // Create mock UserResponse DTOs
        UserResponse userResponse1 = mockUserResponse(user1);
        UserResponse userResponse2 = mockUserResponse(user2);
        List<UserResponse> userResponseList = List.of(userResponse1, userResponse2);
        // Create a mock PageResponse of UserResponse
        PageResponse<UserResponse> pageResponse = mockPageResponse(userResponseList);

        // Define mock behavior for service and mapper
        // userService.findAllUsers should be called with the search criteria (filter) and Pageable
        doReturn(usersPage).when(userService).findAllUsers(eq(filter));
        doReturn(pageResponse).when(userMapper).toPageResponse(any(Page.class));

        // Act & Assert
        mockMvc.perform(get(BASE_USERS_URL)
                        .param("page", "0") // Default pagination params if not explicitly in criteria DTO
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)) // Content-Type for request, not always needed for GET but good practice
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User list retrieved."))
                .andExpect(jsonPath("$.data.content.length()").value(pageResponse.pageSize())) // Assert number of items
                .andExpect(jsonPath("$.data.totalElements").value(pageResponse.totalElements()))
                .andExpect(jsonPath("$.data.totalPages").value(pageResponse.totalPages()));
    }

    @Test
    @DisplayName("Should return a list of users successfully")
    void testGetUsers_WithFilter_shouldReturnAUsers() throws Exception {
        // Arrange
        UserSearchCriteria filter = new UserSearchCriteria();
        filter.setName("John");

        // Create mock User entities
        User user = mockUser();
        List<User> mockUsers = List.of(user);
        // Create a mock Page of Users
        Page<User> usersPage = new PageImpl<>(mockUsers);
        when(userService.findAllUsers(any(UserSearchCriteria.class))).thenReturn(usersPage);

        // Create mock UserResponse DTOs
        PageResponse<UserResponse> pageResponse = mockPageResponse(List.of(mockUserResponse(user)));

        doReturn(usersPage).when(userService).findAllUsers(eq(filter));
        doReturn(pageResponse).when(userMapper).toPageResponse(any(Page.class));

        // Act & Assert
        mockMvc.perform(get(BASE_USERS_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("name", filter.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User list retrieved."))
                .andExpect(jsonPath("$.data.content.length()").value(pageResponse.pageSize())) // Assert number of items
                .andExpect(jsonPath("$.data.totalElements").value(pageResponse.totalElements()))
                .andExpect(jsonPath("$.data.totalPages").value(pageResponse.totalPages()));
    }

    private User mockUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("StrongPassword123!");

        return user;
    }

    private UserUpdateRequest createUserUpdateRequestObject() {
        return new UserUpdateRequest(
                "Johnny",
                "Doey",
                "TestCompany",
                "1234",
                "profile_url");
    }

    private UserResponse mockUserResponse(User user) {
        return new UserResponse(
            user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCompany(),
                user.getPhoneNumber(),
                user.isActive(),
                user.getDeactivatedAt(),
                user.getProfileImageUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private PageResponse<UserResponse> mockPageResponse(List<UserResponse> userResponseList) {
        return new PageResponse<>(
                userResponseList, 0, userResponseList.size(), userResponseList.size(), 1, true);
    }
}
