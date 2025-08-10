package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private AuthService verificationService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // Define the default image URL (mocking @Value injection)
    private static final String DEFAULT_IMAGE_URL = "http://example.com/default.jpg";

    @BeforeEach
    void setUp() {
        // Manually set the @Value injected field for testing

    }

    @Test
    @DisplayName("Should successfully create a user")
    void testCreateUser_ShouldPass() {
        // Arrange
        User userModel = mockUser();

        // Mock the save operation
        User savedUser = mockSavedUser();

        doReturn(savedUser).when(userRepository).save(any(User.class));

        // Act
        User resultUser = userService.createUser(userModel);

        // Assertions
        assertNotNull(resultUser);
        assertNotNull(resultUser.getId());
        assertEquals(userModel.getEmail(), resultUser.getEmail());
        assertEquals(DEFAULT_IMAGE_URL, resultUser.getProfileImageUrl());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if email is not unique")
    void testValidateEmailIsUnique_EmailIsNotUnique_ThrowsException() {
        String existingEmail = "john.doe@example.com";

        // Create a mock existing user
        User existingUser = mockSavedUser();

        // Mock userRepository.findByEmail to return Optional.of(existingUser)
        when(userRepository.findByEmail(eq(existingEmail))).thenReturn(Optional.of(existingUser));

        // Act & Assert
        DuplicateResourceException thrown = assertThrows(DuplicateResourceException.class, () -> {
            userService.validateEmailIsUnique(existingEmail);
        });

        assertEquals(
                "This email is already registered. Please login or reset your password.",
                thrown.getMessage());

        // Verify that getUserByEmail (via userRepository) was called
        verify(userRepository, times(1)).findByEmail(eq(existingEmail));
    }

    @Test
    @DisplayName("Should do nothing if email is unique")
    void testValidateEmailIsUnique_EmailIsUnique_NoExceptionThrown() {
        String uniqueEmail = "unique@example.com";

        // Mock userRepository.findByEmail to return Optional.empty()
        // Assuming getUserByEmail calls userRepository.findByEmail
        when(userRepository.findByEmail(eq(uniqueEmail))).thenReturn(Optional.empty());

        // Act & Assert (no exception should be thrown)
        userService.validateEmailIsUnique(uniqueEmail);

        // Verify that getUserByEmail (via userRepository) was called
        verify(userRepository, times(1)).findByEmail(eq(uniqueEmail));
    }

    @Test
    @DisplayName("Should return a Page of users based on size")
    void testFindAllUsers_WithoutFilter_ShouldReturnUsers() {
        // Arrange
        UserSearchCriteria criteria = new UserSearchCriteria();
        Pageable expectedPageable = criteria.toPageable();

        // Create mock User entities
        User user1 = mockSavedUser();
        User user2 = mockSavedUser();
        user2.setId(UUID.randomUUID());
        user2.setEmail("sara.smith@example.com");
        user2.setFirstName("Sarah");
        user2.setLastName("Smith");
        List<User> userList = List.of(user1, user2);

        // Create a mock Page of Users that userRepository.findAll would return
        Page<User> expectedUsersPage = new PageImpl<>(userList, expectedPageable, 2);

        // Mock the userRepository.findAll method
        // We use any(Specification.class) because UserSpecification is instantiated internally
        // and we don't want to rely on its equals() method for the mock match.
        // We use eq(expectedPageable) because Pageable is a well-behaved object for equality.
        when(userRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(expectedUsersPage);

        // Act
        Page<User> resultPage = userService.findAllUsers(criteria);

        // Assertions
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(userList.size(), resultPage.getContent().size());
        assertEquals(user1.getEmail(), resultPage.getContent().get(0).getEmail());
        assertEquals(user2.getEmail(), resultPage.getContent().get(1).getEmail());
        assertEquals(expectedPageable.getPageNumber(), resultPage.getPageable().getPageNumber());
        assertEquals(expectedPageable.getPageSize(), resultPage.getPageable().getPageSize());
        assertEquals(expectedPageable.getSort(), resultPage.getPageable().getSort());
    }

    @Test
    @DisplayName("Should return a Page of users based on filter")
    void testFindAllUsers_WithFilter_ShouldReturnUsers() {
        // Arrange
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setName("John"); //filter by name
        Pageable expectedPageable = criteria.toPageable();

        User user = mockSavedUser();
        List<User> userList = List.of(user);

        Page<User> expectedUsersPage = new PageImpl<>(userList, expectedPageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(expectedUsersPage);

        // Act
        Page<User> resultPage = userService.findAllUsers(criteria);

        // Assertions
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(userList.size(), resultPage.getContent().size());
        assertEquals(user.getEmail(), resultPage.getContent().getFirst().getEmail());
        assertEquals(expectedPageable.getPageNumber(), resultPage.getPageable().getPageNumber());
        assertEquals(expectedPageable.getPageSize(), resultPage.getPageable().getPageSize());
        assertEquals(expectedPageable.getSort(), resultPage.getPageable().getSort());
    }

    @Test
    @DisplayName("Should return an empty Page when no users match criteria")
    void testFindAllUsers_ShouldReturnEmptyPage_WhenNoMatches() {
        // Arrange
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setActive(false);
        Pageable expectedPageable = criteria.toPageable();

        // Mock an empty Page of Users
        Page<User> emptyUsersPage = new PageImpl<>(new ArrayList<>(), expectedPageable, 0);

        // Mock the userRepository.findAll method to return an empty page
        when(userRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(emptyUsersPage);

        // Act
        Page<User> resultPage = userService.findAllUsers(criteria);

        // Assertions
        assertNotNull(resultPage);
        assertEquals(0, resultPage.getTotalElements());
        assertEquals(0, resultPage.getContent().size());

        // Verify interactions
        verify(userRepository, times(1)).findAll(
                any(Specification.class), // Verify a Specification was passed
                eq(expectedPageable)
        );
    }

    private User mockUser() {
        User userModel = new User();
        userModel.setEmail("john.doe@example.com");
        userModel.setFirstName("John");
        userModel.setLastName("Doe");
        userModel.setPassword("hashedPassword");

        return userModel;
    }

    private User mockSavedUser() {
        User savedUser = mockUser();
        savedUser.setId(UUID.randomUUID());
        savedUser.setProfileImageUrl(DEFAULT_IMAGE_URL);
        savedUser.setActive(true);

        return savedUser;
    }
}
