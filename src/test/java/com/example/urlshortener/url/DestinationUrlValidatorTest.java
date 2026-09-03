package com.example.urlshortener.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class DestinationUrlValidatorTest {

    private final DestinationUrlValidator validator = new DestinationUrlValidator();

    @ParameterizedTest
    @MethodSource("validUrls")
    void acceptsAndPreservesApprovedUrls(String url) {
        assertEquals(url, validator.validate(url));
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void rejectsProhibitedUrls(String url) {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(url));
    }

    @org.junit.jupiter.api.Test
    void rejectsNullDestination() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
    }

    @org.junit.jupiter.api.Test
    void acceptsMaximumLengthUrl() {
        String prefix = "https://example.com/";
        String url = prefix + "a".repeat(DestinationUrlValidator.MAX_LENGTH - prefix.length());
        assertEquals(DestinationUrlValidator.MAX_LENGTH, url.length());
        assertEquals(url, validator.validate(url));
    }

    static Stream<Arguments> validUrls() {
        return Stream.of(
                arguments("https://example.com/articles/architecture?source=demo#part"),
                arguments("HTTP://localhost:8080/path"),
                arguments("https://xn--bcher-kva.example/%E2%9C%93"),
                arguments("http://127.0.0.1/resource"),
                arguments("http://[2001:db8::1]:65535/path"),
                arguments("https://example.com/a/../b?x=%2F"));
    }

    static Stream<Arguments> invalidUrls() {
        return Stream.of(
                arguments(""),
                arguments("https://example.com/" + "a".repeat(4_096)),
                arguments("ftp://example.com/file"),
                arguments("/relative/path"),
                arguments("https://example.com" + "a".repeat(4_087)),
                arguments("https://user:pass@example.com/path"),
                arguments("https://example.com/a b"),
                arguments("https://example.com/%0a"),
                arguments("https://example.com/%zz"),
                arguments("https://bücher.example/path"),
                arguments("https://example.com:0/path"),
                arguments("https://example.com:65536/path"),
                arguments("https://-example.com/path"),
                arguments("https://example..com/path"));
    }
}
