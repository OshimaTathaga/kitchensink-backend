package com.mongodb.kitchensink.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record ErrorDetails(
        @JsonInclude(JsonInclude.Include.NON_NULL) String field,
        @JsonInclude(JsonInclude.Include.NON_NULL) String message
) {
}
