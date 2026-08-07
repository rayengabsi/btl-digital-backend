package com.btl.digital.mock.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ApiError {

    private final boolean success = false;
    private final String message;
    private final String errorCode;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> details;
    private final String timestamp;

    private ApiError(String message, String errorCode, List<String> details) {
        this.message = message;
        this.errorCode = errorCode;
        this.details = details == null ? List.of() : details;
        this.timestamp = Instant.now().toString();
    }

    public static ApiError of(String message, String errorCode) {
        return new ApiError(message, errorCode, List.of());
    }

    public static ApiError of(String message, String errorCode, List<String> details) {
        return new ApiError(message, errorCode, details);
    }
}