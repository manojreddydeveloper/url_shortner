package com.example.urlshortener.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.urlshortener.web.RequestCorrelationFilter;

@RestControllerAdvice
public final class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_CODE = "INTERNAL_ERROR";
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred.";

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                exception.code(),
                exception.getMessage(),
                RequestCorrelationFilter.from(request),
                exception.details());
        return ResponseEntity.status(exception.status()).body(response);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        LOGGER.atError()
                .addKeyValue("operation", "request")
                .addKeyValue("outcome", "unexpected_failure")
                .addKeyValue("exceptionType", exception.getClass().getName())
                .log("Unhandled request failure");

        ApiErrorResponse response = ApiErrorResponse.of(
                INTERNAL_ERROR_CODE,
                INTERNAL_ERROR_MESSAGE,
                RequestCorrelationFilter.from(request),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
