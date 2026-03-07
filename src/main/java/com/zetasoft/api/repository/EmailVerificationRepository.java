package com.zetasoft.api.repository;

import com.zetasoft.api.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
    Optional<EmailVerification> findByEmailAndUsedFalse(String email);
    void deleteByEmail(String email);
}
