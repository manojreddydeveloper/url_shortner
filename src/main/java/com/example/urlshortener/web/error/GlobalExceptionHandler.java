package com.example.urlshortener.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

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

    @ExceptionHandler(RateLimitException.class)
    org.springframework.http.ResponseEntity<ApiErrorResponse> handleRateLimitException(
            RateLimitException exception,
            jakarta.servlet.http.HttpServletResponse httpResponse) {
        httpResponse.setHeader(
                org.springframework.http.HttpHeaders.RETRY_AFTER,
                String.valueOf(exception.retryAfterSeconds()));
        ApiErrorResponse response = ApiErrorResponse.of(
                "RATE_LIMITED",
                "Rate limit exceeded. Please retry after "
                        + exception.retryAfterSeconds() + " seconds.",
                "unavailable",
                null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                "METHOD_NOT_ALLOWED",
                "The requested method is not supported.",
                RequestCorrelationFilter.from(request),
                null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                "UNSUPPORTED_MEDIA_TYPE",
                "The request media type is not supported.",
                RequestCorrelationFilter.from(request),
                null);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        Throwable cause = exception.getMostSpecificCause();
        String causeMessage = cause == null ? "" : String.valueOf(cause.getMessage());
        String exceptionMessage = String.valueOf(exception.getMessage());
        boolean unknownField = causeMessage.toLowerCase().contains("unrecognized field")
                || causeMessage.toLowerCase().contains("unknown property")
                || exceptionMessage.toLowerCase().contains("unrecognized field")
                || exceptionMessage.toLowerCase().contains("unknown property");
        String code = unknownField ? "VALIDATION_ERROR" : "INVALID_JSON";
        String message = unknownField
                ? "The request contains invalid fields."
                : "The request body is not valid JSON.";
        ApiErrorResponse response = ApiErrorResponse.of(
                code,
                message,
                RequestCorrelationFilter.from(request),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
