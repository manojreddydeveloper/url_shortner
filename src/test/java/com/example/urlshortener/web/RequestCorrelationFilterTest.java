package com.example.urlshortener.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesSafeRequestIdAndClearsMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "client-request_123");
        AtomicReference<String> requestAttribute = new AtomicReference<>();
        AtomicReference<String> mdcValue = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            requestAttribute.set(RequestCorrelationFilter.from(request));
            mdcValue.set(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY));
        });

        assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
                .isEqualTo("client-request_123");
        assertThat(requestAttribute).hasValue("client-request_123");
        assertThat(mdcValue).hasValue("client-request_123");
        assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void replacesUnsafeRequestIdAndRestoresPreviousMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "unsafe\nvalue");
        MDC.put(RequestCorrelationFilter.REQUEST_ID_MDC_KEY, "outer-request");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String generated = RequestCorrelationFilter.from(request);
            assertThat(generated)
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                    .doesNotContain("unsafe");
        });

        assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
                .doesNotContain("unsafe");
        assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY))
                .isEqualTo("outer-request");
    }
}
