package com.example.urlshortener.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.urlshortener.web.RequestCorrelationFilter;

@ExtendWith(OutputCaptureExtension.class)
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestCorrelationFilter())
                .build();
    }

    @Test
    void mapsExpectedExceptionToStableEnvelope() throws Exception {
        mockMvc.perform(get("/expected")
                        .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "request-123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(
                        RequestCorrelationFilter.REQUEST_ID_HEADER,
                        "request-123"))
                .andExpect(jsonPath("$.error.code").value("TEST_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Safe test message."))
                .andExpect(jsonPath("$.error.requestId").value("request-123"))
                .andExpect(jsonPath("$.error.details").isEmpty());
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromClientsAndLogs(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/unexpected")
                        .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "request-456"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.error.requestId").value("request-456"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive-database-detail"))));

        assertThat(output.getAll()).doesNotContain("sensitive-database-detail");
    }

    @Test
    void handlesRateLimitExceptionWith429AndRetryAfter() throws Exception {
        mockMvc.perform(get("/rate-limited"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.error.message").value(
                        org.hamcrest.Matchers.containsString("Rate limit exceeded")));
    }

    @Test
    void handlesApiExceptionWithDetails() throws Exception {
        mockMvc.perform(get("/with-details")
                        .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "req-d"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.error.details[0].field").value("url"));
    }

    @RestController
    static class TestController {

        @GetMapping("/expected")
        void expected() {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TEST_ERROR", "Safe test message.");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("sensitive-database-detail");
        }

        @GetMapping("/rate-limited")
        void rateLimited() {
            throw new RateLimitException(30);
        }

        @GetMapping("/with-details")
        void withDetails() {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Invalid fields.", List.of(new ApiErrorResponse.ErrorDetail("url", "must be valid")));
        }
    }
}
