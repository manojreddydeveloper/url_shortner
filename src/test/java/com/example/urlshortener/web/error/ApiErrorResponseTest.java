package com.example.urlshortener.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.urlshortener.web.error.ApiErrorResponse.ErrorBody;
import com.example.urlshortener.web.error.ApiErrorResponse.ErrorDetail;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {

    @Test
    void storesErrorBody() {
        ErrorBody body = new ErrorBody("CODE", "msg", "req-1", List.of());
        ApiErrorResponse response = new ApiErrorResponse(body);
        assertThat(response.error()).isEqualTo(body);
    }

    @Test
    void rejectsNullErrorBody() {
        assertThatThrownBy(() -> new ApiErrorResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("error");
    }

    @Test
    void factoryMethodCreatesResponse() {
        ApiErrorResponse response = ApiErrorResponse.of("CODE", "msg", "req-1", List.of());
        assertThat(response.error().code()).isEqualTo("CODE");
        assertThat(response.error().message()).isEqualTo("msg");
        assertThat(response.error().requestId()).isEqualTo("req-1");
        assertThat(response.error().details()).isEmpty();
    }

    @Test
    void nullDetailsInErrorBodyDefaultsToEmptyList() {
        ErrorBody body = new ErrorBody("CODE", "msg", "req-1", null);
        assertThat(body.details()).isEmpty();
    }

    @Test
    void copiesDetailsList() {
        java.util.List<ErrorDetail> details = new java.util.ArrayList<>();
        details.add(new ErrorDetail("f", "r"));
        ErrorBody body = new ErrorBody("CODE", "msg", "req-1", details);
        assertThat(body.details()).containsExactly(new ErrorDetail("f", "r"));
        assertThat(body.details()).isNotSameAs(details);
    }
}
