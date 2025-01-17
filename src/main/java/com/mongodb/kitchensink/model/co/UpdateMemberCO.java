package com.mongodb.kitchensink.model.co;

import com.mongodb.kitchensink.document.Member;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.mongodb.kitchensink.constant.KitchensinkConstant.PASSWORD_VALIDATION_REGEX;
import static com.mongodb.kitchensink.constant.KitchensinkConstant.PHONE_NUMBER_VALIDATION_REGEX;

@Builder
public record UpdateMemberCO(String name,
                             @Pattern(regexp = PASSWORD_VALIDATION_REGEX, message = "Password must be at least 8 characters long, contain at least one letter, and one number") String password,
                             @Pattern(regexp = PHONE_NUMBER_VALIDATION_REGEX, message = "Phone number must be exactly 10 digits and start with 6, 7, 8, or 9") String phoneNumber) {
    public void merge(PasswordEncoder passwordEncoder, Member base) {
        Optional.ofNullable(this.name).ifPresent(base::setName);
        Optional.ofNullable(this.password).map(passwordEncoder::encode).ifPresent(base::setPassword);
        Optional.ofNullable(this.phoneNumber).ifPresent(base::setPhoneNumber);
    }
}
