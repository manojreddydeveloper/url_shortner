package com.example.urlshortener.url;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class LinkWriterTest {
    @Test
    void isolatesEachCollisionAttemptInANewTransaction() throws Exception {
        Transactional transaction = LinkWriter.class
                .getMethod("save", com.example.urlshortener.persistence.LinkEntity.class)
                .getAnnotation(Transactional.class);

        assertEquals(Propagation.REQUIRES_NEW, transaction.propagation());
    }
}
