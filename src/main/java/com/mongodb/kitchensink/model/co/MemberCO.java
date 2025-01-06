package com.mongodb.kitchensink.model.co;

import com.mongodb.kitchensink.document.Member;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Builder
public record MemberCO(String email, String name, String password, List<String> roles, String phoneNumber) {
    public Member to(PasswordEncoder passwordEncoder) {
        return Member
                .builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .phoneNumber(phoneNumber)
                .build();
    }
}
