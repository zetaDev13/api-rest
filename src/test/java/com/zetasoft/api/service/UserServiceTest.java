package com.zetasoft.api.service;

import com.zetasoft.api.exception.ConflictException;
import com.zetasoft.api.exception.ResourceNotFoundException;
import com.zetasoft.api.mapper.UserMapper;
import com.zetasoft.api.model.dto.UserRequest;
import com.zetasoft.api.model.dto.UserResponse;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest testRequest;
    private UserResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = UserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .build();

        testResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .active(true)
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Should return all users")
    void findAll_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by id")
    void findById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testResponse);

        UserResponse result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void findById_ShouldThrowException_WhenNotExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with99'");
    id: ' }

    @Test
    @DisplayName("Should create new user")
    void create_ShouldCreateUser() {
        when(userRepository.existsByUsername(testRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(testRequest)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testResponse);

        UserResponse result = userService.create(testRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should throw ConflictException when username exists")
    void create_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername(testRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(testRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists with username");
    }

    @Test
    @DisplayName("Should update existing user")
    void update_ShouldUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(testRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        UserResponse result = userService.update(1L, testRequest);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user")
    void delete_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void delete_ShouldThrowException_WhenNotExists() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
