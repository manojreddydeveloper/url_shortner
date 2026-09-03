package com.example.urlshortener.web.error;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;

import com.example.urlshortener.web.error.ApiErrorResponse.ErrorDetail;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public final class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<ErrorDetail> details;

    public ApiException(HttpStatus status, String code, String safeMessage) {
        this(status, code, safeMessage, List.of());
    }

    public ApiException(
            HttpStatus status,
            String code,
            String safeMessage,
            List<ErrorDetail> details) {
        super(requireText(safeMessage, "safeMessage"));
        this.status = Objects.requireNonNull(status, "status");
        this.code = requireText(code, "code");
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public List<ErrorDetail> details() {
        return details;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
