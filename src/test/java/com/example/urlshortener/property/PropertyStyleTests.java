package com.example.urlshortener.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.urlshortener.url.DestinationUrlValidator;
import com.example.urlshortener.url.ShortCodeGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PropertyStyleTests {

    private final DestinationUrlValidator validator = new DestinationUrlValidator();
    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    // --- DestinationUrlValidator: valid URLs always pass ---

    @ParameterizedTest(name = "valid URL passes: {0}")
    @MethodSource("validUrls")
    void validUrlsAlwaysAccepted(String url) {
        assertThat(validator.validate(url)).isEqualTo(url);
    }

    static Stream<Arguments> validUrls() {
        return Stream.of(
                Arguments.of("https://example.com"),
                Arguments.of("http://localhost"),
                Arguments.of("https://example.com:8080/path"),
                Arguments.of("http://192.168.1.1"),
                Arguments.of("https://[::1]"),
                Arguments.of("https://sub.domain.example.com"),
                Arguments.of("https://example.com/path?q=1&r=2"),
                Arguments.of("https://example.com/path#fragment")
        );
    }

    // --- DestinationUrlValidator: invalid URLs always rejected ---

    @ParameterizedTest(name = "invalid URL rejected: {0}")
    @ValueSource(strings = {
            "", "not-a-url", "ftp://example.com", "javascript:alert(1)",
            "https://user:pass@example.com", "https://example.com/path\r\ninjection",
            "https://example.com:0", "https://example.com:65536",
            "https://example.com:99999"
    })
    void invalidUrlsAlwaysRejected(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- ShortCodeGenerator: output always 10 Base62 chars ---

    @ParameterizedTest(name = "generated code #{index}")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void generatedCodeIsAlwaysTenBase62Chars(int ignored) {
        String code = generator.generate();
        assertThat(code).hasSize(10);
        assertThat(code).matches("[0-9A-Za-z]{10}");
    }

    @ParameterizedTest(name = "collision retry boundary: attempt {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void mayRetryCollisionAcceptsValidAttempts(int attempt) {
        assertThat(generator.mayRetryCollision(attempt)).isTrue();
    }

    @ParameterizedTest(name = "collision retry rejects: attempt {0}")
    @ValueSource(ints = {0, 6, 7, 100})
    void mayRetryCollisionRejectsInvalidAttempts(int attempt) {
        assertThat(generator.mayRetryCollision(attempt)).isFalse();
    }
}
