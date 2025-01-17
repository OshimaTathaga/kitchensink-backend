package com.mongodb.kitchensink.initializer;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("local")
public class MemberInitializer {
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeUsers(MemberRepository memberRepository) {
        return args -> {
            if (memberRepository.count() == 0) {
                log.info("Bootstrapping Users in the DB");
                Member admin = Member
                        .builder()
                        .email("admin@kitchensink.com")
                        .password(passwordEncoder.encode("password1234"))
                        .name("KS Admin")
                        .phoneNumber("9876543210")
                        .roles(List.of("ADMIN"))
                        .build();

                Member user1 = Member
                        .builder()
                        .email("user1@kitchensink.com")
                        .password(passwordEncoder.encode("password1234"))
                        .name("KS Normal User 1")
                        .phoneNumber("8876543210")
                        .roles(List.of("USER"))
                        .build();

                Member user2 = Member
                        .builder()
                        .email("user2@kitchensink.com")
                        .password(passwordEncoder.encode("password1234"))
                        .name("KS Normal User 2")
                        .phoneNumber("7876543210")
                        .roles(List.of("USER"))
                        .build();

                memberRepository.saveAll(List.of(admin, user1, user2));
                log.info("Bootstrapping Users Successful");
            }
        };


    }

}
