package com.example.urlshortener.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.urlshortener.config.UrlShortenerProperties;
import com.example.urlshortener.url.LinkCreationService;
import com.example.urlshortener.web.error.GlobalExceptionHandler;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LinkCreationControllerTest {

    private MockMvc mockMvc;
    private LinkCreationService service;

    @BeforeEach
    void setUp() {
        service = mock(LinkCreationService.class);
        UrlShortenerProperties properties = mock(UrlShortenerProperties.class);
        when(properties.publicBaseUrl()).thenReturn(URI.create("https://sho.rt"));
        mockMvc = MockMvcBuilders.standaloneSetup(new LinkCreationController(service, properties))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsCreatedWithFullResponseShape() throws Exception {
        when(service.create("https://example.com")).thenReturn(
                new LinkCreationService.Result("aZ3kP9mQ2x", "https://example.com",
                        Instant.parse("2026-01-01T00:00:00Z"), "tok_abc123"));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("aZ3kP9mQ2x"))
                .andExpect(jsonPath("$.shortUrl").value("https://sho.rt/aZ3kP9mQ2x"))
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.analyticsToken").value("tok_abc123"));
    }

    @Test
    void rejectsNullUrl() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsMissingUrlField() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void mapsValidationRejectionTo400() throws Exception {
        when(service.create("not-a-url")).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"not-a-url\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void mapsCreationUnavailableTo500() throws Exception {
        when(service.create("https://example.com"))
                .thenThrow(new LinkCreationService.CreationUnavailableException());

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    void mapsDependencyUnavailableTo503() throws Exception {
        when(service.create("https://example.com"))
                .thenThrow(new LinkCreationService.DependencyUnavailableException());

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("DEPENDENCY_UNAVAILABLE"));
    }
}
