package com.mongodb.kitchensink.initializer;

import com.mongodb.kitchensink.document.User;
import com.mongodb.kitchensink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInitializer {
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Bootstrapping Users in the DB");
                User admin = User.builder().username("admin")
                        .password(passwordEncoder.encode("password"))
                        .roles(List.of("ADMIN"))
                        .build();

                User user = User.builder().username("user")
                        .password(passwordEncoder.encode("password"))
                        .roles(List.of("USER"))
                        .build();

                userRepository.saveAll(List.of(admin, user));
                log.info("Bootstrapping Users Successful");
            }
        };


    }

}
