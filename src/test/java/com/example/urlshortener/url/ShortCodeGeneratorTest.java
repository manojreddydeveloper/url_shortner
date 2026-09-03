package com.example.urlshortener.url;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {
    @Test
    void generatesApprovedFormat() {
        ShortCodeGenerator generator = new ShortCodeGenerator();
        for (int i = 0; i < 1_000; i++) {
            String code = generator.generate();
            assertEquals(ShortCodeGenerator.LENGTH, code.length());
            assertTrue(code.chars().allMatch(c -> ShortCodeGenerator.ALPHABET.indexOf(c) >= 0));
        }
    }

    @Test
    void producesIndependentCandidatesDeterministically() {
        SecureRandom random = new SecureRandom() {
            private int value;
            @Override public int nextInt(int bound) { return value++ % 248; }
        };
        ShortCodeGenerator generator = new ShortCodeGenerator(random);
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10; i++) codes.add(generator.generate());
        assertEquals(10, codes.size());
    }

    @Test
    void usesRejectionSamplingForUnbiasedBytes() {
        SecureRandom random = new SecureRandom() {
            private final int[] values = {255, 0, 61, 62, 247};
            private int index;
            @Override public int nextInt(int bound) { return values[index++ % values.length]; }
        };
        String code = new ShortCodeGenerator(random).generate();
        assertEquals("0z0z0z0z0z", code);
    }

    @Test
    void permitsInitialAttemptAndFiveCollisionRetriesOnly() {
        ShortCodeGenerator generator = new ShortCodeGenerator();
        assertTrue(generator.mayRetryCollision(1));
        assertTrue(generator.mayRetryCollision(5));
        assertFalse(generator.mayRetryCollision(0));
        assertFalse(generator.mayRetryCollision(6));
    }
}
