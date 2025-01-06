package com.mongodb.kitchensink.model.co;

import com.mongodb.kitchensink.document.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.mongodb.kitchensink.constant.KitchensinkConstant.EMAIL_VALIDATION_REGEX;

@Builder
public record MemberCO(@Email(regexp = EMAIL_VALIDATION_REGEX) @NotEmpty String email, @NotBlank String name, @NotBlank String password, @NotEmpty List<@NotBlank String> roles, String phoneNumber) {
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
