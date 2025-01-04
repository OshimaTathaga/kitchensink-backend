package com.mongodb.kitchensink.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mongodb.kitchensink.error.AppError;
import com.mongodb.kitchensink.error.ErrorCode;
import com.mongodb.kitchensink.error.ErrorDetails;
import com.mongodb.kitchensink.error.KitchenSinkException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";

    @ExceptionHandler(KitchenSinkException.class)
    public ResponseEntity<AppError> handleKitchenSinkException(KitchenSinkException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity
                .status(ex.getErrorCode().getHttpCode())
                .body(AppError.builder()
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<AppError> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getHttpCode())
                .body(AppError.builder()
                        .code(ErrorCode.NOT_FOUND)
                        .message("Resource not found")
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn(ex.getMessage(), ex);
        List<ErrorDetails> errorDetails = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(e -> (FieldError) e)
                .map(e -> new ErrorDetails(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .badRequest()
                .body(AppError.builder()
                        .code(ErrorCode.VALIDATION_ERROR)
                        .message(VALIDATION_FAILED)
                        .details(errorDetails)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            log.warn(ex.getMessage(), ex);
            List<ErrorDetails> details = invalidFormatException.getPath()
                    .stream()
                    .map(path -> new ErrorDetails(path.getFieldName(), "Invalid value"))
                    .collect(Collectors.toList());
            return ResponseEntity
                    .badRequest()
                    .body(AppError.builder()
                            .code(ErrorCode.VALIDATION_ERROR)
                            .details(details)
                            .message(VALIDATION_FAILED)
                            .build());
        }
        return handleException(ex);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<AppError> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn(ex.getMessage(), ex);
        List<ErrorDetails> errorDetails = ex.getParameterValidationResults()
                .stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .flatMap(Collection::stream)
                .map(e -> ErrorDetails.builder().message(e.getDefaultMessage()).build())
                .toList();
        return ResponseEntity
                .badRequest()
                .body(AppError.builder()
                        .code(ErrorCode.VALIDATION_ERROR)
                        .message(VALIDATION_FAILED)
                        .details(errorDetails)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AppError> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn(ex.getMessage(), ex);
        List<ErrorDetails> errorDetails = ex.getConstraintViolations()
                .stream()
                .map(e -> new ErrorDetails(e.getPropertyPath().toString(), e.getMessage()))
                .toList();
        return ResponseEntity
                .badRequest()
                .body(AppError.builder()
                        .code(ErrorCode.VALIDATION_ERROR)
                        .message(VALIDATION_FAILED)
                        .details(errorDetails)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AppError> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getHttpCode())
                .body(AppError.builder()
                        .code(ErrorCode.UNAUTHORIZED)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppError> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .internalServerError()
                .body(AppError.builder()
                        .code(ErrorCode.SERVER_ERROR)
                        .message("Server error")
                        .build());
    }
}
