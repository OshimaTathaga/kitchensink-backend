package com.mongodb.kitchensink.error;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public enum ErrorCode {
    VALIDATION_ERROR(400),
    UNAUTHENTICATED(401),
    UNAUTHORIZED(403),
    NOT_FOUND(404),

    SERVER_ERROR(500);

    int httpCode;
}