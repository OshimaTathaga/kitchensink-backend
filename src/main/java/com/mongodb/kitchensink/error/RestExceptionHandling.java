package com.mongodb.kitchensink.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandling {
    private final ObjectMapper objectMapper;

    public void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        this.setErrorResponse(response, null, errorCode, message);
    }

    public void setErrorResponse(HttpServletResponse response, KitchenSinkException kitchenSinkException) throws IOException {
        log.warn(kitchenSinkException.getMessage(), kitchenSinkException);
        AppError error = AppError.builder()
                .code(kitchenSinkException.getErrorCode())
                .message(kitchenSinkException.getMessage())
                .build();

        response.setStatus(kitchenSinkException.getErrorCode().getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }

    public void setErrorResponse(HttpServletResponse response, Throwable throwable, ErrorCode errorCode) throws IOException {
        this.setErrorResponse(response, throwable, errorCode, throwable.getMessage());
    }

    public void setErrorResponse(HttpServletResponse response, Throwable throwable, ErrorCode errorCode, String message) throws IOException {
        log.warn(message, throwable);
        AppError error = AppError.builder()
                .code(errorCode)
                .message(message)
                .build();

        response.setStatus(errorCode.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}