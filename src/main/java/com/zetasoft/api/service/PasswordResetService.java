package com.zetasoft.api.service;

import com.zetasoft.api.exception.ResourceNotFoundException;
import com.zetasoft.api.model.entity.PasswordReset;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.PasswordResetRepository;
import com.zetasoft.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;

    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        passwordResetRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);

        PasswordReset passwordReset = PasswordReset.builder()
                .email(email)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetRepository.save(passwordReset);
    }

    @Transactional
    public void confirmReset(String token, String newPassword) {
        PasswordReset passwordReset = passwordResetRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Password reset", "token", token));

        if (passwordReset.getUsed()) {
            throw new IllegalStateException("Password reset token already used");
        }

        if (passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Password reset token expired");
        }

        User user = userRepository.findByEmail(passwordReset.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", passwordReset.getEmail()));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordReset.setUsed(true);
        passwordResetRepository.save(passwordReset);
    }
}
