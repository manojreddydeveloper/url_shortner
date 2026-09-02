package com.example.urlshortener.url;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/** Generates unpredictable, case-sensitive Base62 short codes. */
@Component
public final class ShortCodeGenerator {
    public static final int LENGTH = 10;
    public static final int MAX_ATTEMPTS = 6;
    public static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ACCEPTED_RANDOM_BYTE_BOUND = 248;
    private final SecureRandom random;

    public ShortCodeGenerator() { this(new SecureRandom()); }

    ShortCodeGenerator(SecureRandom random) { this.random = random; }

    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        while (code.length() < LENGTH) {
            int value = random.nextInt(256);
            if (value < ACCEPTED_RANDOM_BYTE_BOUND) {
                code.append(ALPHABET.charAt(value % ALPHABET.length()));
            }
        }
        return code.toString();
    }

    public boolean mayRetryCollision(int attemptNumber) {
        return attemptNumber >= 1 && attemptNumber < MAX_ATTEMPTS;
    }
}
