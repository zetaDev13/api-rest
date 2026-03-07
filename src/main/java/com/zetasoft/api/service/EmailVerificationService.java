package com.zetasoft.api.service;

import com.zetasoft.api.exception.ResourceNotFoundException;
import com.zetasoft.api.model.entity.EmailVerification;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.EmailVerificationRepository;
import com.zetasoft.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    private static final int TOKEN_EXPIRATION_MINUTES = 60 * 24;

    @Transactional
    public void requestVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }

        emailVerificationRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        emailVerificationRepository.save(verification);
    }

    @Transactional
    public void confirmVerification(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Email verification", "token", token));

        if (verification.getUsed()) {
            throw new IllegalStateException("Email verification token already used");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Email verification token expired");
        }

        User user = userRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", verification.getEmail()));

        user.setEmailVerified(true);
        userRepository.save(user);

        verification.setUsed(true);
        emailVerificationRepository.save(verification);
    }
}
