package com.mongodb.kitchensink.model.co;

import com.mongodb.kitchensink.document.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.mongodb.kitchensink.constant.KitchensinkConstant.*;

@Builder
public record MemberCO(@Email(regexp = EMAIL_VALIDATION_REGEX) @NotEmpty String email,
                       @NotBlank String name,
                       @NotBlank @Pattern(regexp = PASSWORD_VALIDATION_REGEX, message = "Password must be at least 8 characters long, contain at least one letter, and one number") String password,
                       @Pattern(regexp = PHONE_NUMBER_VALIDATION_REGEX, message = "Phone number must be exactly 10 digits and start with 6, 7, 8, or 9") String phoneNumber) {
    public Member to(PasswordEncoder passwordEncoder, List<String> roles) {
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
