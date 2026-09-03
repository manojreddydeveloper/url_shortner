package com.example.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class UrlShortenerApplicationTest {

    @Test
    void applicationEntryPointDeclaresSpringBootApplication() {
        assertThat(UrlShortenerApplication.class)
                .hasAnnotation(SpringBootApplication.class);
    }
}
