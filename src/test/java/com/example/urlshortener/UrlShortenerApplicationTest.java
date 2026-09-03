package com.example.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class UrlShortenerApplicationTest {

    @Test
    void applicationEntryPointDeclaresSpringBootApplication() {
        assertThat(UrlShortenerApplication.class)
                .hasAnnotation(SpringBootApplication.class);
    }
}
