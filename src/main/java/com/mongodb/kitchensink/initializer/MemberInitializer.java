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
                        .password(passwordEncoder.encode("password"))
                        .name("KS Admin")
                        .phoneNumber("+91XXXXXXXXXX")
                        .roles(List.of("ADMIN"))
                        .build();

                Member user = Member
                        .builder()
                        .email("user@kitchensink.com")
                        .password(passwordEncoder.encode("password"))
                        .name("KS Normal User")
                        .phoneNumber("+91XXXXXXXXXX")
                        .roles(List.of("USER"))
                        .build();

                memberRepository.saveAll(List.of(admin, user));
                log.info("Bootstrapping Users Successful");
            }
        };


    }

}
