package com.zetasoft.api.service;

import com.zetasoft.api.exception.ResourceNotFoundException;
import com.zetasoft.api.model.entity.EmailVerification;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.EmailVerificationRepository;
import com.zetasoft.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;
    private EmailVerification emailVerification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .name("Test User")
                .active(true)
                .emailVerified(false)
                .build();

        emailVerification = EmailVerification.builder()
                .id(1L)
                .email("test@example.com")
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .used(false)
                .build();
    }

    @Test
    @DisplayName("Should request email verification successfully")
    void requestVerification_ShouldCreateToken() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(emailVerificationRepository.save(any(EmailVerification.class))).thenReturn(emailVerification);

        emailVerificationService.requestVerification("test@example.com");

        verify(emailVerificationRepository, times(1)).deleteByEmail("test@example.com");
        verify(emailVerificationRepository, times(1)).save(any(EmailVerification.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void requestVerification_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.requestVerification("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    @DisplayName("Should throw exception when email already verified")
    void requestVerification_ShouldThrowException_WhenAlreadyVerified() {
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> emailVerificationService.requestVerification("test@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    @DisplayName("Should confirm email verification successfully")
    void confirmVerification_ShouldMarkEmailAsVerified() {
        when(emailVerificationRepository.findByToken("valid-token")).thenReturn(Optional.of(emailVerification));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailVerificationRepository.save(any(EmailVerification.class))).thenReturn(emailVerification);

        emailVerificationService.confirmVerification("valid-token");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when token not found")
    void confirmVerification_ShouldThrowException_WhenTokenNotFound() {
        when(emailVerificationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.confirmVerification("invalid-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email verification not found with token");
    }

    @Test
    @DisplayName("Should throw exception when token already used")
    void confirmVerification_ShouldThrowException_WhenTokenAlreadyUsed() {
        emailVerification.setUsed(true);
        when(emailVerificationRepository.findByToken("valid-token")).thenReturn(Optional.of(emailVerification));

        assertThatThrownBy(() -> emailVerificationService.confirmVerification("valid-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already used");
    }

    @Test
    @DisplayName("Should throw exception when token expired")
    void confirmVerification_ShouldThrowException_WhenTokenExpired() {
        emailVerification.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        when(emailVerificationRepository.findByToken("valid-token")).thenReturn(Optional.of(emailVerification));

        assertThatThrownBy(() -> emailVerificationService.confirmVerification("valid-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }
}
