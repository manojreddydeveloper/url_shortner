package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.example.urlshortener.persistence.LinkRepository;

class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    LinkRepository linkRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    void concurrentCreationsAllSucceed() throws Exception {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        Set<String> codes = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> request = new HttpEntity<>(
                            "{\"url\":\"https://example.com/" + index + "\"}", headers);
                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            "http://localhost:" + port + "/api/v1/links", request, Map.class);
                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        codes.add(response.getBody().get("code").toString());
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(codes).hasSize(threadCount);
        assertThat(linkRepository.count()).isEqualTo(threadCount);
    }

    @Test
    void concurrentCreationsWithSameDestinationProduceDistinctMappings() throws Exception {
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        Set<String> codes = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> request = new HttpEntity<>(
                            "{\"url\":\"https://same-destination.com\"}", headers);
                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            "http://localhost:" + port + "/api/v1/links", request, Map.class);
                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        codes.add(response.getBody().get("code").toString());
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(codes).hasSize(threadCount);
    }
}
