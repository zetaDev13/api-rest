package com.zetasoft.api.service;

import com.zetasoft.api.exception.ResourceNotFoundException;
import com.zetasoft.api.model.entity.PasswordReset;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.PasswordResetRepository;
import com.zetasoft.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordReset passwordReset;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .name("Test User")
                .active(true)
                .build();

        passwordReset = PasswordReset.builder()
                .id(1L)
                .email("test@example.com")
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
    }

    @Test
    @DisplayName("Should request password reset successfully")
    void requestReset_ShouldCreateToken() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetRepository.save(any(PasswordReset.class))).thenReturn(passwordReset);

        passwordResetService.requestReset("test@example.com");

        verify(passwordResetRepository, times(1)).deleteByEmail("test@example.com");
        verify(passwordResetRepository, times(1)).save(any(PasswordReset.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found for password reset")
    void requestReset_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.requestReset("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    @DisplayName("Should confirm password reset successfully")
    void confirmReset_ShouldUpdatePassword() {
        when(passwordResetRepository.findByToken("valid-token")).thenReturn(Optional.of(passwordReset));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordResetRepository.save(any(PasswordReset.class))).thenReturn(passwordReset);

        passwordResetService.confirmReset("valid-token", "newpassword");

        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when token not found")
    void confirmReset_ShouldThrowException_WhenTokenNotFound() {
        when(passwordResetRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.confirmReset("invalid-token", "newpassword"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Password reset not found with token");
    }

    @Test
    @DisplayName("Should throw exception when token already used")
    void confirmReset_ShouldThrowException_WhenTokenAlreadyUsed() {
        passwordReset.setUsed(true);
        when(passwordResetRepository.findByToken("valid-token")).thenReturn(Optional.of(passwordReset));

        assertThatThrownBy(() -> passwordResetService.confirmReset("valid-token", "newpassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already used");
    }

    @Test
    @DisplayName("Should throw exception when token expired")
    void confirmReset_ShouldThrowException_WhenTokenExpired() {
        passwordReset.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        when(passwordResetRepository.findByToken("valid-token")).thenReturn(Optional.of(passwordReset));

        assertThatThrownBy(() -> passwordResetService.confirmReset("valid-token", "newpassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }
}
