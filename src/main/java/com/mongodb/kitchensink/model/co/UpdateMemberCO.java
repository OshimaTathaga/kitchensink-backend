package com.mongodb.kitchensink.model.co;

import com.mongodb.kitchensink.document.Member;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Builder
public record UpdateMemberCO(String name, String password, String phoneNumber) {
    public void merge(PasswordEncoder passwordEncoder, Member base) {
        Optional.ofNullable(this.name).ifPresent(base::setName);
        Optional.ofNullable(this.password).map(passwordEncoder::encode).ifPresent(base::setPassword);
        Optional.ofNullable(this.phoneNumber).ifPresent(base::setPhoneNumber);
    }
}
