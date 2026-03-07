package com.zetasoft.api.config;

import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@zetasoft.com")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Administrator")
                    .active(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
