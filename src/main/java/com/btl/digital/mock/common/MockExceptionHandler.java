package com.btl.digital.mock.common;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Converts mock business errors and bean-validation errors into the documented
 * error envelope for /api/v1.
 */
@RestControllerAdvice(basePackages = "com.btl.digital.mock")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MockExceptionHandler {

    @ExceptionHandler(MockApiException.class)
    public ResponseEntity<ApiError> handleMockApi(MockApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getMessage(), ex.getErrorCode(), ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("Validation failed", "VALIDATION_ERROR", details));
    }
}