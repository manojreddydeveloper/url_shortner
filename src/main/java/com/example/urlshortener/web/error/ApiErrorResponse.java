package com.example.urlshortener.web.error;

import java.util.List;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public record ApiErrorResponse(ErrorBody error) {

    public ApiErrorResponse {
        if (error == null) {
            throw new IllegalArgumentException("error is required");
        }
    }

    public static ApiErrorResponse of(
            String code,
            String message,
            String requestId,
            List<ErrorDetail> details) {
        return new ApiErrorResponse(new ErrorBody(code, message, requestId, details));
    }

    public record ErrorBody(
            String code,
            String message,
            String requestId,
            List<ErrorDetail> details) {

        public ErrorBody {
            details = details == null ? List.of() : List.copyOf(details);
        }
    }

    public record ErrorDetail(String field, String reason) {
    }
}
