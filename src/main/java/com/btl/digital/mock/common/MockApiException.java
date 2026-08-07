package com.btl.digital.mock.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class MockApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final List<String> details;

    public MockApiException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, List.of());
    }

    public MockApiException(HttpStatus status, String errorCode, String message, List<String> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }
}