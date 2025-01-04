package com.mongodb.kitchensink.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
public record AppError(
        ErrorCode code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ErrorDetails> details
) {
}
