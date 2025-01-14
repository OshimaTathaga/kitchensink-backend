package com.mongodb.kitchensink.model.co;

import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Builder
public record UpdateMemberCO(String name, String password, String phoneNumber) {
    public UpdateMemberCO obfuscatePassword(PasswordEncoder passwordEncoder) {
        return UpdateMemberCO.builder()
                .name(this.name)
                .password(Objects.nonNull(this.password) ? passwordEncoder.encode(this.password): null)
                .phoneNumber(this.phoneNumber)
                .build();

    }
}
