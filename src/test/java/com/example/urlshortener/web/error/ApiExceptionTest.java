package com.example.urlshortener.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.urlshortener.web.error.ApiErrorResponse.ErrorDetail;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionTest {

    @Test
    void storesStatusAndCodeAndSafeMessage() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "CODE", "Safe message.");
        assertThat(ex.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.code()).isEqualTo("CODE");
        assertThat(ex.getMessage()).isEqualTo("Safe message.");
        assertThat(ex.details()).isEmpty();
    }

    @Test
    void storesDetailsList() {
        List<ErrorDetail> details = List.of(new ErrorDetail("field", "reason"));
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "CODE", "msg", details);
        assertThat(ex.details()).containsExactly(new ErrorDetail("field", "reason"));
    }

    @Test
    void nullDetailsBecomesEmptyList() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "CODE", "msg", null);
        assertThat(ex.details()).isEmpty();
    }

    @Test
    void rejectsNullStatus() {
        assertThatThrownBy(() -> new ApiException(null, "CODE", "msg"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> new ApiException(HttpStatus.BAD_REQUEST, "  ", "msg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code");
    }

    @Test
    void rejectsNullCode() {
        assertThatThrownBy(() -> new ApiException(HttpStatus.BAD_REQUEST, null, "msg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankSafeMessage() {
        assertThatThrownBy(() -> new ApiException(HttpStatus.BAD_REQUEST, "CODE", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("safeMessage");
    }

    @Test
    void rejectsNullSafeMessage() {
        assertThatThrownBy(() -> new ApiException(HttpStatus.BAD_REQUEST, "CODE", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
